#!/usr/bin/env python3
import argparse
import csv
import json
from pathlib import Path


PROFILES = ["baseline", "tracing", "logging_extended", "tracing_logging_extended"]


def parse_timing(path: Path):
    values = {}
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if "=" not in line:
                continue
            key, raw = line.split("=", 1)
            key = key.strip()
            raw = raw.strip().replace("%", "")
            try:
                values[key] = float(raw)
            except ValueError:
                values[key] = None
    return values


def delta(base, value):
    if base is None or value is None:
        return None
    return value - base


def pct(base, value):
    if base in (None, 0) or value is None:
        return None
    return (value - base) / base * 100.0


def write_report(path: Path, rows):
    lines = [
        "# Lab5 CI Monitoring Comparison",
        "",
        "| profile | user sec | system sec | max rss (MB) | elapsed sec | user delta % | rss delta % |",
        "|---|---:|---:|---:|---:|---:|---:|",
    ]
    for row in rows:
        rss_mb = row["max_rss_kb"] / 1024 if row.get("max_rss_kb") is not None else None
        lines.append(
            f"| {row['profile']} | {fmt(row.get('user_seconds'))} | {fmt(row.get('system_seconds'))} | "
            f"{fmt(rss_mb)} | {fmt(row.get('elapsed_seconds'))} | {fmt(row.get('user_delta_pct'))} | "
            f"{fmt(row.get('rss_delta_pct'))} |"
        )
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def fmt(value):
    if value is None:
        return "n/a"
    return f"{value:.2f}"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input-dir", required=True)
    parser.add_argument("--out-dir", required=True)
    args = parser.parse_args()

    input_dir = Path(args.input_dir)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    rows = []
    for profile in PROFILES:
        timing_file = input_dir / f"{profile}-timing.txt"
        if not timing_file.exists():
            raise SystemExit(f"missing timing file: {timing_file}")
        metrics = parse_timing(timing_file)
        metrics["profile"] = profile
        rows.append(metrics)

    baseline = next(row for row in rows if row["profile"] == "baseline")
    for row in rows:
        row["user_delta_pct"] = pct(baseline.get("user_seconds"), row.get("user_seconds"))
        row["rss_delta_pct"] = pct(baseline.get("max_rss_kb"), row.get("max_rss_kb"))

    fields = [
        "profile",
        "user_seconds",
        "system_seconds",
        "cpu_percent",
        "max_rss_kb",
        "elapsed_seconds",
        "user_delta_pct",
        "rss_delta_pct",
    ]
    with open(out_dir / "lab5_ci_monitoring.csv", "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        w.writerows(rows)

    with open(out_dir / "lab5_ci_monitoring.json", "w", encoding="utf-8") as f:
        json.dump({"profiles": rows}, f, indent=2)

    write_report(out_dir / "lab5_ci_monitoring_report.md", rows)


if __name__ == "__main__":
    main()
