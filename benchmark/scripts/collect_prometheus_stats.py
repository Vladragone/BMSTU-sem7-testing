#!/usr/bin/env python3
import argparse
import csv
import datetime as dt
import json
import re
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


def fetch_metric_with_fallback(
    prom_url: str,
    metric_name: str,
    service: str,
    compose_project: str,
    container_id: str,
    start: int,
    end: int,
):
    cfg = METRICS[metric_name]
    metric = cfg["metric"]

    def match_service(labels):
        svc = service.lower()
        project = (compose_project or "").lower()
        cid = (container_id or "").lower()

        compose_service = str(labels.get("container_label_com_docker_compose_service", "")).lower()
        compose_project_label = str(labels.get("container_label_com_docker_compose_project", "")).lower()

        # Strongest match: exact compose project + exact service.
        if project and compose_project_label == project and compose_service == svc:
            return True

        # If project isn't available in labels, still accept exact service match.
        if compose_service == svc and (not project or not compose_project_label):
            return True

        # Strong fallback by container id fragment if available.
        if cid:
            for key in ("id", "name", "container"):
                v = str(labels.get(key, "")).lower()
                if cid in v:
                    return True

        # Fallback heuristic by common container labels.
        candidates = [
            labels.get("name", ""),
            labels.get("container", ""),
            labels.get("id", ""),
            labels.get("image", ""),
        ]
        if project:
            scoped_pattern = re.compile(rf"{re.escape(project)}.*(^|[-_/\.]){re.escape(svc)}($|[-_/\.])")
            for c in candidates:
                v = str(c).lower()
                if scoped_pattern.search(v):
                    return True
        pattern = re.compile(rf"(^|[-_/\.]){re.escape(svc)}($|[-_/\.])")
        for c in candidates:
            v = str(c).lower()
            if pattern.search(v):
                return True
        return False

    queries = []
    if cfg["rate"]:
        queries.append(f"rate({metric}[30s])")
        queries.append(f"rate({metric}{{container!=\"\"}}[30s])")
    else:
        queries.append(metric)
        queries.append(f"{metric}{{container!=\"\"}}")

    # Try direct matcher-based queries first, then generic + client-side filtering.
    for expr in _query_candidates(metric, service, cfg["rate"]):
        q = f"({expr}) * {cfg['scale']}" if cfg["scale"] != 1.0 else expr
        queries.append(q)

    for query in queries:
        result = fetch_range(prom_url, query, start, end)
        if not result:
            continue
        filtered = [s for s in result if match_service(s.get("metric", {}))]
        if filtered:
            if cfg["scale"] != 1.0 and query in (f"rate({metric}[30s])", f"rate({metric}{{container!=\"\"}}[30s])", metric, f"{metric}{{container!=\"\"}}"):
                # Apply scale on values if expression wasn't multiplied in PromQL.
                for series in filtered:
                    new_values = []
                    for ts, raw_value in series.get("values", []):
                        try:
                            new_values.append([ts, str(float(raw_value) * cfg["scale"])])
                        except ValueError:
                            new_values.append([ts, raw_value])
                    series["values"] = new_values
            return filtered
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


def plot_service_metric(path: Path, service: str, rows, metric_key: str, title: str, y_label: str, color: str):
    fig, ax = plt.subplots(figsize=(14, 5), dpi=140)
    ax.set_title(title)
    ax.set_xlabel("Time")
    ax.set_ylabel(y_label)
    ax.grid(True, alpha=0.3)

    if rows:
        x = [dt.datetime.fromisoformat(r["timestamp"]) for r in rows]
        if metric_key == "ram_mb":
            y = [(r.get("ram_bytes") or 0.0) / (1024 * 1024) for r in rows]
        else:
            y = [r.get(metric_key) or 0.0 for r in rows]
        ax.plot(x, y, label=y_label, color=color, linewidth=1.4)
        ax.legend()
        fig.autofmt_xdate(rotation=20)
    else:
        ax.text(0.5, 0.5, "No data from Prometheus for this service", ha="center", va="center", transform=ax.transAxes)

    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--prom-url", required=True)
    parser.add_argument("--start", type=int, required=True)
    parser.add_argument("--end", type=int, required=True)
    parser.add_argument("--services", nargs="+", default=["app", "db"])
    parser.add_argument("--compose-project", default="")
    parser.add_argument("--container-id", action="append", default=[])
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    out_path = Path(args.out)
    run_dir = out_path.parent
    container_id_by_service = {}
    for item in args.container_id:
        if "=" not in item:
            continue
        svc, cid = item.split("=", 1)
        container_id_by_service[svc.strip()] = cid.strip().lower()

    report = {
        "start_unix": args.start,
        "end_unix": args.end,
        "components": {},
    }

    for service in args.services:
        series_by_metric = {}
        summary_by_metric = {}

        for metric_name in METRICS.keys():
            raw_series = fetch_metric_with_fallback(
                args.prom_url,
                metric_name,
                service,
                args.compose_project,
                container_id_by_service.get(service, ""),
                args.start,
                args.end,
            )
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
        plot_service_metric(
            run_dir / f"resources_{service}_cpu_over_time.png",
            service,
            rows,
            "cpu_percent",
            f"CPU Utilization Over Time ({service})",
            "CPU, %",
            "#1f77b4",
        )
        plot_service_metric(
            run_dir / f"resources_{service}_ram_over_time.png",
            service,
            rows,
            "ram_mb",
            f"RAM Utilization Over Time ({service})",
            "RAM, MB",
            "#ff7f0e",
        )

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)


if __name__ == "__main__":
    main()
