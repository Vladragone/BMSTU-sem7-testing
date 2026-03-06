#!/usr/bin/env python3
import argparse
import csv
from pathlib import Path

try:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
except Exception as exc:  # pragma: no cover
    raise SystemExit(f"matplotlib is required for plotting: {exc}")


def read_latency_ts(path: Path):
    rows = []
    with open(path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for i, row in enumerate(reader):
            rows.append(
                {
                    "t": i,
                    "avg": float(row["avg_latency_ms"]) if row["avg_latency_ms"] else 0.0,
                    "p95": float(row["p95_latency_ms"]) if row["p95_latency_ms"] else 0.0,
                }
            )
    return rows


def read_percentiles(path: Path):
    data = {}
    with open(path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            data[float(row["percentile"])] = float(row["latency_ms"]) if row["latency_ms"] else 0.0
    return data


def read_hist(path: Path):
    centers = []
    counts = []
    with open(path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            lo = float(row["bin_start_ms"])
            hi = float(row["bin_end_ms"])
            c = float(row["count"])
            centers.append((lo + hi) / 2.0)
            counts.append(c)
    return centers, counts


def read_resources_ts(path: Path):
    rows = []
    with open(path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for i, row in enumerate(reader):
            rows.append(
                {
                    "t": i,
                    "cpu": float(row["cpu_percent"]) if row["cpu_percent"] else 0.0,
                    "ram_mb": (float(row["ram_bytes"]) / (1024 * 1024)) if row["ram_bytes"] else 0.0,
                    "disk_r": float(row["disk_read_bytes_per_s"]) if row["disk_read_bytes_per_s"] else 0.0,
                    "disk_w": float(row["disk_write_bytes_per_s"]) if row["disk_write_bytes_per_s"] else 0.0,
                }
            )
    return rows


def plot_latency_over_time(old_rows, new_rows, out_path: Path, label_old: str, label_new: str):
    fig, ax = plt.subplots(figsize=(14, 6), dpi=140)
    ax.plot([r["t"] for r in old_rows], [r["p95"] for r in old_rows], label=f"{label_old} p95", linewidth=1.6)
    ax.plot([r["t"] for r in new_rows], [r["p95"] for r in new_rows], label=f"{label_new} p95", linewidth=1.6)
    ax.plot([r["t"] for r in old_rows], [r["avg"] for r in old_rows], label=f"{label_old} avg", linewidth=1.0, alpha=0.7)
    ax.plot([r["t"] for r in new_rows], [r["avg"] for r in new_rows], label=f"{label_new} avg", linewidth=1.0, alpha=0.7)
    ax.set_title("Latency Over Time: GC Comparison")
    ax.set_xlabel("Time index (seconds)")
    ax.set_ylabel("Latency, ms")
    ax.grid(True, alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)


def plot_latency_metric_two_lines(old_rows, new_rows, metric: str, title: str, out_path: Path, label_old: str, label_new: str):
    fig, ax = plt.subplots(figsize=(14, 6), dpi=140)
    ax.plot([r["t"] for r in old_rows], [r[metric] for r in old_rows], label=label_old, linewidth=1.8)
    ax.plot([r["t"] for r in new_rows], [r[metric] for r in new_rows], label=label_new, linewidth=1.8)
    ax.set_title(title)
    ax.set_xlabel("Time index (seconds)")
    ax.set_ylabel("Latency, ms")
    ax.grid(True, alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)


def plot_percentiles(old_p, new_p, out_path: Path, label_old: str, label_new: str):
    ps = [0.5, 0.75, 0.9, 0.95, 0.99]
    labels = [f"p{int(p * 100)}" for p in ps]
    old_vals = [old_p.get(p, 0.0) for p in ps]
    new_vals = [new_p.get(p, 0.0) for p in ps]
    fig, ax = plt.subplots(figsize=(10, 5), dpi=140)
    ax.plot(labels, old_vals, marker="o", label=label_old, linewidth=1.6)
    ax.plot(labels, new_vals, marker="o", label=label_new, linewidth=1.6)
    ax.set_title("Latency Percentiles: GC Comparison")
    ax.set_xlabel("Percentile")
    ax.set_ylabel("Latency, ms")
    ax.grid(True, alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)


def plot_hist(old_hist, new_hist, out_path: Path, label_old: str, label_new: str):
    old_x, old_y = old_hist
    new_x, new_y = new_hist
    fig, ax = plt.subplots(figsize=(12, 5), dpi=140)
    ax.plot(old_x, old_y, label=label_old, linewidth=1.3)
    ax.plot(new_x, new_y, label=label_new, linewidth=1.3)
    ax.set_title("Latency Histogram: GC Comparison")
    ax.set_xlabel("Latency bin center, ms")
    ax.set_ylabel("Request count")
    ax.grid(True, alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)


def plot_resource(old_rows, new_rows, metric: str, ylabel: str, title: str, out_path: Path, label_old: str, label_new: str):
    fig, ax = plt.subplots(figsize=(14, 5), dpi=140)
    ax.plot([r["t"] for r in old_rows], [r[metric] for r in old_rows], label=label_old, linewidth=1.5)
    ax.plot([r["t"] for r in new_rows], [r[metric] for r in new_rows], label=label_new, linewidth=1.5)
    ax.set_title(title)
    ax.set_xlabel("Time index (samples)")
    ax.set_ylabel(ylabel)
    ax.grid(True, alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(out_path)
    plt.close(fig)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--old-run-dir", required=True)
    parser.add_argument("--new-run-dir", required=True)
    parser.add_argument("--out-dir", required=True)
    parser.add_argument("--label-old", default="old_gc")
    parser.add_argument("--label-new", default="new_gc")
    args = parser.parse_args()

    old_dir = Path(args.old_run_dir)
    new_dir = Path(args.new_run_dir)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    old_latency_ts = read_latency_ts(old_dir / "latency_timeseries.csv")
    new_latency_ts = read_latency_ts(new_dir / "latency_timeseries.csv")
    plot_latency_over_time(old_latency_ts, new_latency_ts, out_dir / "compare_latency_over_time.png", args.label_old, args.label_new)
    plot_latency_metric_two_lines(
        old_latency_ts,
        new_latency_ts,
        "p95",
        "Latency p95 Over Time: GC Comparison",
        out_dir / "compare_latency_p95_over_time.png",
        args.label_old,
        args.label_new,
    )
    plot_latency_metric_two_lines(
        old_latency_ts,
        new_latency_ts,
        "avg",
        "Latency avg Over Time: GC Comparison",
        out_dir / "compare_latency_avg_over_time.png",
        args.label_old,
        args.label_new,
    )

    old_p = read_percentiles(old_dir / "latency_percentiles.csv")
    new_p = read_percentiles(new_dir / "latency_percentiles.csv")
    plot_percentiles(old_p, new_p, out_dir / "compare_latency_percentiles.png", args.label_old, args.label_new)

    old_hist = read_hist(old_dir / "latency_histogram.csv")
    new_hist = read_hist(new_dir / "latency_histogram.csv")
    plot_hist(old_hist, new_hist, out_dir / "compare_latency_histogram.png", args.label_old, args.label_new)

    for service in ("app", "db"):
        old_res = read_resources_ts(old_dir / f"resources_timeseries_{service}.csv")
        new_res = read_resources_ts(new_dir / f"resources_timeseries_{service}.csv")
        plot_resource(
            old_res,
            new_res,
            "cpu",
            "CPU, %",
            f"CPU Over Time ({service}): GC Comparison",
            out_dir / f"compare_{service}_cpu_over_time.png",
            args.label_old,
            args.label_new,
        )
        plot_resource(
            old_res,
            new_res,
            "ram_mb",
            "RAM, MB",
            f"RAM Over Time ({service}): GC Comparison",
            out_dir / f"compare_{service}_ram_over_time.png",
            args.label_old,
            args.label_new,
        )
        plot_resource(
            old_res,
            new_res,
            "disk_r",
            "Disk read, B/s",
            f"Disk Read Over Time ({service}): GC Comparison",
            out_dir / f"compare_{service}_disk_read_over_time.png",
            args.label_old,
            args.label_new,
        )
        plot_resource(
            old_res,
            new_res,
            "disk_w",
            "Disk write, B/s",
            f"Disk Write Over Time ({service}): GC Comparison",
            out_dir / f"compare_{service}_disk_write_over_time.png",
            args.label_old,
            args.label_new,
        )


if __name__ == "__main__":
    main()
