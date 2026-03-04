#!/usr/bin/env python3
import argparse
import csv
import json
from pathlib import Path


PROFILES = [
    "baseline",
    "tracing",
    "logging_extended",
    "tracing_logging_extended",
]


def read_json(path: Path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def extract_metrics(campaign_dir: Path):
    summary = read_json(campaign_dir / "final_summary.json")
    metrics = summary.get("metrics", {})
    return {
        "http_p95_ms": metrics.get("http_p95_ms", {}).get("median"),
        "http_p99_ms": metrics.get("http_p99_ms", {}).get("median"),
        "http_failed_rate": metrics.get("http_failed_rate", {}).get("median"),
        "app_cpu_median": metrics.get("app_cpu_median", {}).get("median"),
        "app_ram_median_bytes": metrics.get("app_ram_median", {}).get("median"),
        "db_cpu_median": metrics.get("db_cpu_median", {}).get("median"),
        "db_ram_median_bytes": metrics.get("db_ram_median", {}).get("median"),
    }


def delta(base, value):
    if base is None or value is None:
        return None
    return value - base


def pct(base, value):
    if base in (None, 0) or value is None:
        return None
    return (value - base) / base * 100.0


def write_csv(path: Path, rows):
    fields = [
        "profile",
        "campaign_dir",
        "http_p95_ms",
        "http_p95_delta_ms",
        "http_p95_delta_pct",
        "app_cpu_median",
        "app_cpu_delta",
        "app_cpu_delta_pct",
        "app_ram_median_bytes",
        "app_ram_delta_bytes",
        "app_ram_delta_pct",
    ]
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        w.writerows(rows)


def write_report(path: Path, rows):
    lines = [
        "# Lab5 Resource Comparison",
        "",
        "| profile | p95 (ms) | p95 delta % | app cpu median | app cpu delta % | app ram median (MB) | app ram delta % |",
        "|---|---:|---:|---:|---:|---:|---:|",
    ]
    for row in rows:
        ram_mb = row["app_ram_median_bytes"] / (1024 * 1024) if row["app_ram_median_bytes"] is not None else None
        lines.append(
            f"| {row['profile']} | "
            f"{format_num(row['http_p95_ms'])} | {format_num(row['http_p95_delta_pct'])} | "
            f"{format_num(row['app_cpu_median'])} | {format_num(row['app_cpu_delta_pct'])} | "
            f"{format_num(ram_mb)} | {format_num(row['app_ram_delta_pct'])} |"
        )
    lines.extend(
        [
            "",
            "## Key deltas",
            f"- Tracing overhead vs baseline (CPU % median): {format_num(find_pct(rows, 'tracing', 'app_cpu_delta_pct'))}",
            f"- Tracing overhead vs baseline (RAM % median): {format_num(find_pct(rows, 'tracing', 'app_ram_delta_pct'))}",
            f"- Extended logging overhead vs baseline (CPU % median): {format_num(find_pct(rows, 'logging_extended', 'app_cpu_delta_pct'))}",
            f"- Extended logging overhead vs baseline (RAM % median): {format_num(find_pct(rows, 'logging_extended', 'app_ram_delta_pct'))}",
        ]
    )
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def format_num(value):
    if value is None:
        return "n/a"
    return f"{value:.2f}"


def find_pct(rows, profile, key):
    for row in rows:
        if row["profile"] == profile:
            return row.get(key)
    return None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--results-root", required=True)
    parser.add_argument("--matrix-stamp", required=True)
    parser.add_argument("--out-dir", required=True)
    args = parser.parse_args()

    results_root = Path(args.results_root)
    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    collected = []
    for profile in PROFILES:
        campaign_dir = results_root / f"{args.matrix_stamp}-{profile}"
        if not (campaign_dir / "final_summary.json").exists():
            raise SystemExit(f"missing final_summary.json for profile '{profile}' in {campaign_dir}")
        metrics = extract_metrics(campaign_dir)
        metrics["profile"] = profile
        metrics["campaign_dir"] = str(campaign_dir)
        collected.append(metrics)

    baseline = next(row for row in collected if row["profile"] == "baseline")
    rows = []
    for row in collected:
        rows.append(
            {
                **row,
                "http_p95_delta_ms": delta(baseline["http_p95_ms"], row["http_p95_ms"]),
                "http_p95_delta_pct": pct(baseline["http_p95_ms"], row["http_p95_ms"]),
                "app_cpu_delta": delta(baseline["app_cpu_median"], row["app_cpu_median"]),
                "app_cpu_delta_pct": pct(baseline["app_cpu_median"], row["app_cpu_median"]),
                "app_ram_delta_bytes": delta(baseline["app_ram_median_bytes"], row["app_ram_median_bytes"]),
                "app_ram_delta_pct": pct(baseline["app_ram_median_bytes"], row["app_ram_median_bytes"]),
            }
        )

    write_csv(out_dir / "lab5_benchmark_comparison.csv", rows)
    write_report(out_dir / "lab5_benchmark_report.md", rows)

    with open(out_dir / "lab5_benchmark_summary.json", "w", encoding="utf-8") as f:
        json.dump({"matrix_stamp": args.matrix_stamp, "profiles": rows}, f, indent=2)


if __name__ == "__main__":
    main()
