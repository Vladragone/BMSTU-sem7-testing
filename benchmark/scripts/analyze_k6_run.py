#!/usr/bin/env python3
import argparse
import csv
import datetime as dt
import json
import math
import re
from collections import defaultdict
from pathlib import Path

try:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
except Exception as exc:  # pragma: no cover
    raise SystemExit(f"matplotlib is required for plotting: {exc}")


TARGET_PERCENTILES = [0.5, 0.75, 0.9, 0.95, 0.99]


def percentile(values, p):
    if not values:
        return None
    ordered = sorted(values)
    rank = (len(ordered) - 1) * p
    low = math.floor(rank)
    high = math.ceil(rank)
    if low == high:
        return ordered[low]
    return ordered[low] + (ordered[high] - ordered[low]) * (rank - low)


def parse_duration_seconds(value):
    text = str(value).strip()
    if text.isdigit():
        return int(text)
    m = re.fullmatch(r"(\d+)\s*([smh])", text)
    if not m:
        raise ValueError(f"Unsupported duration format: {value}")
    num = int(m.group(1))
    unit = m.group(2)
    if unit == "s":
        return num
    if unit == "m":
        return num * 60
    if unit == "h":
        return num * 3600
    raise ValueError(f"Unsupported duration unit: {unit}")


def read_points(path, scenario_name, drop_warmup_seconds):
    raw = []
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                item = json.loads(line)
            except json.JSONDecodeError:
                # k6 json output can contain occasional malformed lines;
                # skip them to keep report generation resilient.
                continue
            if item.get("type") != "Point":
                continue
            if item.get("metric") != "http_req_duration":
                continue
            data = item.get("data", {})
            tags = data.get("tags", {})
            if scenario_name and tags.get("scenario") != scenario_name:
                continue
            ts_raw = data.get("time")
            value = data.get("value")
            if ts_raw is None or value is None:
                continue
            ts = dt.datetime.fromisoformat(ts_raw.replace("Z", "+00:00")).replace(microsecond=0)
            raw.append((ts, float(value)))

    if not raw:
        return [], defaultdict(list)

    raw.sort(key=lambda x: x[0])
    if drop_warmup_seconds > 0:
        cutoff = raw[0][0] + dt.timedelta(seconds=drop_warmup_seconds)
        raw = [x for x in raw if x[0] >= cutoff]

    durations = [v for _, v in raw]
    per_second = defaultdict(list)
    for ts, latency in raw:
        per_second[ts].append(latency)
    return durations, per_second


def write_timeseries_csv(path, per_second):
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["timestamp", "request_count", "avg_latency_ms", "p95_latency_ms"])
        for ts in sorted(per_second.keys()):
            values = per_second[ts]
            w.writerow([
                ts.isoformat(),
                len(values),
                sum(values) / len(values),
                percentile(values, 0.95),
            ])


def write_percentiles_csv(path, durations):
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["percentile", "latency_ms"])
        for p in TARGET_PERCENTILES:
            w.writerow([p, percentile(durations, p)])


def write_histogram_csv(path, durations, bins=40):
    if not durations:
        with open(path, "w", newline="", encoding="utf-8") as f:
            csv.writer(f).writerow(["bin_start_ms", "bin_end_ms", "count"])
        return [], []
    lo = min(durations)
    hi = max(durations)
    step = (hi - lo) / bins if hi > lo else 1.0
    counts = [0 for _ in range(bins)]
    for d in durations:
        idx = int((d - lo) / step) if step > 0 else 0
        if idx >= bins:
            idx = bins - 1
        counts[idx] += 1
    edges = [lo + i * step for i in range(bins + 1)]
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["bin_start_ms", "bin_end_ms", "count"])
        for i in range(bins):
            w.writerow([edges[i], edges[i + 1], counts[i]])
    return edges, counts


def build_stage_markers(profile_rps, stage_duration_seconds, transition_seconds):
    markers = []
    offset = 0
    for i, rps in enumerate(profile_rps):
        markers.append((offset, f"RPS {rps}"))
        if i < len(profile_rps) - 1:
            offset += stage_duration_seconds + transition_seconds
    return markers


def plot_latency_over_time(path, per_second, title, drain_start_seconds=None, stage_markers=None):
    ts_sorted = sorted(per_second.keys())
    if not ts_sorted:
        return
    avg = []
    p95 = []
    for ts in ts_sorted:
        values = per_second[ts]
        avg.append(sum(values) / len(values))
        p95.append(percentile(values, 0.95))
    fig, ax = plt.subplots(figsize=(14, 6), dpi=140)
    ax.plot(ts_sorted, avg, label="avg latency (per second)", linewidth=1.2)
    ax.plot(ts_sorted, p95, label="p95 latency (per second)", linewidth=1.2)
    ax.set_title(title)
    ax.set_xlabel("Time")
    ax.set_ylabel("Latency, ms")
    ax.grid(True, alpha=0.3)
    if stage_markers:
        y_top = max(p95) if p95 else 0
        for sec, label in stage_markers:
            idx = int(sec)
            if 0 <= idx < len(ts_sorted):
                t = ts_sorted[idx]
                ax.axvline(t, color="#6c757d", linestyle=":", linewidth=1.1, alpha=0.9)
                ax.text(t, y_top, label, rotation=90, va="bottom", ha="center", fontsize=8, color="#495057")
    if drain_start_seconds is not None and ts_sorted:
        marker_idx = int(drain_start_seconds)
        if 0 <= marker_idx < len(ts_sorted):
            drain_ts = ts_sorted[marker_idx]
            ax.axvline(drain_ts, color="red", linestyle="--", linewidth=1.5, label="drain start")
            ax.scatter([drain_ts], [p95[marker_idx]], color="red", s=20, zorder=5)
    ax.legend()
    fig.autofmt_xdate(rotation=20)
    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def plot_percentiles(path, durations):
    if not durations:
        return
    labels = [f"p{int(p * 100)}" for p in TARGET_PERCENTILES]
    values = [percentile(durations, p) for p in TARGET_PERCENTILES]
    fig, ax = plt.subplots(figsize=(10, 5), dpi=140)
    ax.bar(labels, values, color="#d95f02")
    ax.set_title("Latency Percentiles")
    ax.set_xlabel("Percentile")
    ax.set_ylabel("Latency, ms")
    ax.grid(True, axis="y", alpha=0.3)
    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def plot_histogram(path, durations, bins=40):
    if not durations:
        return
    fig, ax = plt.subplots(figsize=(12, 5), dpi=140)
    ax.hist(durations, bins=bins, color="#1f77b4", edgecolor="black", linewidth=0.4)
    ax.set_title("Latency Distribution Histogram")
    ax.set_xlabel("Latency, ms")
    ax.set_ylabel("Request count")
    ax.grid(True, axis="y", alpha=0.3)
    fig.tight_layout()
    fig.savefig(path)
    plt.close(fig)


def split_by_stage(per_second, stage_duration_seconds, profile_rps):
    ts_sorted = sorted(per_second.keys())
    if not ts_sorted:
        return []
    start = ts_sorted[0]
    stage_count = len(profile_rps)
    stage_buckets = [defaultdict(list) for _ in range(stage_count)]
    for ts in ts_sorted:
        elapsed = int((ts - start).total_seconds())
        stage_idx = elapsed // stage_duration_seconds if stage_duration_seconds > 0 else 0
        if 0 <= stage_idx < stage_count:
            stage_buckets[stage_idx][ts] = per_second[ts]
    return stage_buckets


def plot_stage_graphs(out_dir, per_second, scenario_name, stage_duration_seconds, profile_rps):
    stage_sets = split_by_stage(per_second, stage_duration_seconds, profile_rps)
    for idx, stage_data in enumerate(stage_sets):
        rps = profile_rps[idx]
        if not stage_data:
            continue
        plot_latency_over_time(
            out_dir / f"latency_over_time_rps_{rps}.png",
            stage_data,
            f"Latency Over Time ({scenario_name}, target {rps} rps)",
        )


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--k6-json", required=True)
    parser.add_argument("--out-dir", required=True)
    parser.add_argument("--scenario", default="heavy_overload_recovery")
    parser.add_argument("--drop-warmup-seconds", type=int, default=0)
    parser.add_argument("--stage-duration", default="90s")
    parser.add_argument("--profile-rps", default="100,300,600,900,1200")
    parser.add_argument("--transition-seconds", type=int, default=1)
    parser.add_argument("--drain-start-seconds", type=int, default=None)
    parser.add_argument("--plot-stage-graphs", action="store_true")
    args = parser.parse_args()

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    durations, per_second = read_points(args.k6_json, args.scenario, args.drop_warmup_seconds)
    write_timeseries_csv(out_dir / "latency_timeseries.csv", per_second)
    write_percentiles_csv(out_dir / "latency_percentiles.csv", durations)
    write_histogram_csv(out_dir / "latency_histogram.csv", durations)

    profile_rps = [int(x.strip()) for x in args.profile_rps.split(",") if x.strip()]
    stage_duration_seconds = parse_duration_seconds(args.stage_duration)
    stage_markers = build_stage_markers(profile_rps, stage_duration_seconds, args.transition_seconds)

    plot_latency_over_time(
        out_dir / "latency_over_time.png",
        per_second,
        f"Latency Over Time ({args.scenario})",
        drain_start_seconds=args.drain_start_seconds,
        stage_markers=stage_markers,
    )
    plot_percentiles(out_dir / "latency_percentiles.png", durations)
    plot_histogram(out_dir / "latency_histogram.png", durations)

    if args.plot_stage_graphs:
        plot_stage_graphs(out_dir, per_second, args.scenario, stage_duration_seconds, profile_rps)


if __name__ == "__main__":
    main()
