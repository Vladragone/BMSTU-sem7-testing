#!/usr/bin/env python3
import argparse
import csv
import datetime as dt
import json
import statistics
import urllib.parse
import urllib.request
import urllib.error
from collections import defaultdict
from pathlib import Path

try:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
except Exception as exc:  # pragma: no cover
    raise SystemExit(f"matplotlib is required for plotting: {exc}")


METRICS = {
    "cpu_percent": {
        "metric": "container_cpu_usage_seconds_total",
        "rate": True,
        "scale": 100.0,
    },
    "ram_bytes": {
        "metric": "container_memory_working_set_bytes",
        "rate": False,
        "scale": 1.0,
    },
    "disk_read_bytes_per_s": {
        "metric": "container_fs_reads_bytes_total",
        "rate": True,
        "scale": 1.0,
    },
    "disk_write_bytes_per_s": {
        "metric": "container_fs_writes_bytes_total",
        "rate": True,
        "scale": 1.0,
    },
}


def _query_candidates(metric_name: str, service: str, is_rate: bool):
    safe = service.replace('"', '\\"')
    matchers = [
        f'container_label_com_docker_compose_service="{safe}"',
        f'container_label_com_docker_compose_service=~".*{safe}.*"',
        f'name=~".*{safe}.*"',
        f'container=~".*{safe}.*"',
    ]
    queries = []
    for matcher in matchers:
        if is_rate:
            queries.append(f"rate({metric_name}{{{matcher}}}[30s])")
        else:
            queries.append(f"{metric_name}{{{matcher}}}")
    return queries


def fetch_range(prom_url: str, query: str, start: int, end: int, step: str = "5s"):
    params = urllib.parse.urlencode(
        {
            "query": query,
            "start": start,
            "end": end,
            "step": step,
        }
    )
    url = f"{prom_url.rstrip('/')}/api/v1/query_range?{params}"
    try:
        with urllib.request.urlopen(url, timeout=30) as resp:
            payload = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError:
        return []
    if payload.get("status") != "success":
        return []
    return payload.get("data", {}).get("result", [])


def fetch_metric_with_fallback(prom_url: str, metric_name: str, service: str, start: int, end: int):
    cfg = METRICS[metric_name]
    metric = cfg["metric"]

    for expr in _query_candidates(metric, service, cfg["rate"]):
        query = f"({expr}) * {cfg['scale']}" if cfg["scale"] != 1.0 else expr
        result = fetch_range(prom_url, query, start, end)
        if result:
            return result
    return []


def collapse_series(series):
    by_ts = defaultdict(float)
    for item in series:
        for ts, raw_value in item.get("values", []):
            try:
                by_ts[float(ts)] += float(raw_value)
            except ValueError:
                continue
    return sorted(by_ts.items(), key=lambda x: x[0])


def summarize(values):
    if not values:
        return {"min": None, "max": None, "median": None}
    return {
        "min": min(values),
        "max": max(values),
        "median": statistics.median(values),
    }


def write_service_csv(path: Path, rows):
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["timestamp", "cpu_percent", "ram_bytes", "disk_read_bytes_per_s", "disk_write_bytes_per_s"])
        for row in rows:
            w.writerow(
                [
                    row["timestamp"],
                    row.get("cpu_percent"),
                    row.get("ram_bytes"),
                    row.get("disk_read_bytes_per_s"),
                    row.get("disk_write_bytes_per_s"),
                ]
            )


def plot_service_graph(path: Path, service: str, rows):
    fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(14, 8), dpi=140, sharex=True)
    ax1.set_title(f"Resource Utilization Over Time ({service})")
    ax1.set_ylabel("CPU % / RAM MB")
    ax2.set_xlabel("Time")
    ax2.set_ylabel("Bytes per second")
    ax1.grid(True, alpha=0.3)
    ax2.grid(True, alpha=0.3)

    if rows:
        x = [dt.datetime.fromisoformat(r["timestamp"]) for r in rows]
        cpu = [r.get("cpu_percent") or 0.0 for r in rows]
        ram_mb = [(r.get("ram_bytes") or 0.0) / (1024 * 1024) for r in rows]
        disk_r = [r.get("disk_read_bytes_per_s") or 0.0 for r in rows]
        disk_w = [r.get("disk_write_bytes_per_s") or 0.0 for r in rows]

        ax1.plot(x, cpu, label="CPU %", color="#1f77b4", linewidth=1.2)
        ax1.plot(x, ram_mb, label="RAM MB", color="#ff7f0e", linewidth=1.2)
        ax2.plot(x, disk_r, label="Disk read B/s", color="#2ca02c", linewidth=1.2)
        ax2.plot(x, disk_w, label="Disk write B/s", color="#d62728", linewidth=1.2)
        ax1.legend()
        ax2.legend()
        fig.autofmt_xdate(rotation=20)
    else:
        ax1.text(0.5, 0.5, "No data from Prometheus for this service", ha="center", va="center", transform=ax1.transAxes)
        ax2.text(0.5, 0.5, "No data from Prometheus for this service", ha="center", va="center", transform=ax2.transAxes)

    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--prom-url", required=True)
    parser.add_argument("--start", type=int, required=True)
    parser.add_argument("--end", type=int, required=True)
    parser.add_argument("--services", nargs="+", default=["app", "db"])
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    out_path = Path(args.out)
    run_dir = out_path.parent

    report = {
        "start_unix": args.start,
        "end_unix": args.end,
        "components": {},
    }

    for service in args.services:
        series_by_metric = {}
        summary_by_metric = {}

        for metric_name in METRICS.keys():
            raw_series = fetch_metric_with_fallback(args.prom_url, metric_name, service, args.start, args.end)
            collapsed = collapse_series(raw_series)
            series_by_metric[metric_name] = collapsed
            summary_by_metric[metric_name] = summarize([v for _, v in collapsed])

        report["components"][service] = summary_by_metric

        row_map = {}
        for metric_name, samples in series_by_metric.items():
            for ts, value in samples:
                row_map.setdefault(ts, {})[metric_name] = value

        rows = []
        for ts in sorted(row_map.keys()):
            row = {
                "timestamp": dt.datetime.fromtimestamp(ts, tz=dt.timezone.utc).replace(microsecond=0).isoformat(),
                "cpu_percent": row_map[ts].get("cpu_percent"),
                "ram_bytes": row_map[ts].get("ram_bytes"),
                "disk_read_bytes_per_s": row_map[ts].get("disk_read_bytes_per_s"),
                "disk_write_bytes_per_s": row_map[ts].get("disk_write_bytes_per_s"),
            }
            rows.append(row)

        write_service_csv(run_dir / f"resources_timeseries_{service}.csv", rows)
        plot_service_graph(run_dir / f"resources_{service}_over_time.png", service, rows)

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)


if __name__ == "__main__":
    main()
