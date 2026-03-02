#!/usr/bin/env python3
import argparse
import csv
import glob
import json
import os
import statistics


def safe_get(obj, path, default=None):
    cur = obj
    for p in path:
        if not isinstance(cur, dict) or p not in cur:
            return default
        cur = cur[p]
    return cur


def median(values):
    if not values:
        return None
    return statistics.median(values)


def collect_runs(campaign_dir):
    run_dirs = sorted(glob.glob(os.path.join(campaign_dir, "run-*")))
    rows = []
    for run_dir in run_dirs:
        summary_path = os.path.join(run_dir, "k6-summary.json")
        resources_path = os.path.join(run_dir, "resources-summary.json")
        if not os.path.exists(summary_path):
            continue
        with open(summary_path, "r", encoding="utf-8") as f:
            k6 = json.load(f)
        resources = {}
        if os.path.exists(resources_path):
            with open(resources_path, "r", encoding="utf-8") as f:
                resources = json.load(f)
        row = {
            "run": os.path.basename(run_dir),
            "http_p50_ms": safe_get(k6, ["metrics", "http_req_duration", "values", "p(50)"]),
            "http_p75_ms": safe_get(k6, ["metrics", "http_req_duration", "values", "p(75)"]),
            "http_p90_ms": safe_get(k6, ["metrics", "http_req_duration", "values", "p(90)"]),
            "http_p95_ms": safe_get(k6, ["metrics", "http_req_duration", "values", "p(95)"]),
            "http_p99_ms": safe_get(k6, ["metrics", "http_req_duration", "values", "p(99)"]),
            "http_failed_rate": safe_get(k6, ["metrics", "http_req_failed", "values", "rate"]),
            "app_cpu_median": safe_get(resources, ["components", "app", "cpu_percent", "median"]),
            "app_ram_median": safe_get(resources, ["components", "app", "ram_bytes", "median"]),
            "db_cpu_median": safe_get(resources, ["components", "db", "cpu_percent", "median"]),
            "db_ram_median": safe_get(resources, ["components", "db", "ram_bytes", "median"]),
        }
        rows.append(row)
    return rows


def write_csv(path, rows):
    if not rows:
        return
    fields = list(rows[0].keys())
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        w.writerows(rows)


def build_summary(rows):
    metric_keys = [k for k in rows[0].keys() if k != "run"]
    summary = {
        "runs_total": len(rows),
        "metrics": {},
    }
    for key in metric_keys:
        values = [r[key] for r in rows if r[key] is not None]
        if not values:
            summary["metrics"][key] = {"min": None, "max": None, "median": None}
            continue
        summary["metrics"][key] = {
            "min": min(values),
            "max": max(values),
            "median": median(values),
        }
    return summary


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--campaign-dir", required=True)
    args = parser.parse_args()

    rows = collect_runs(args.campaign_dir)
    if not rows:
        raise SystemExit("No run-* directories with k6-summary.json found")

    write_csv(os.path.join(args.campaign_dir, "runs_summary.csv"), rows)
    final_summary = build_summary(rows)
    with open(os.path.join(args.campaign_dir, "final_summary.json"), "w", encoding="utf-8") as f:
        json.dump(final_summary, f, indent=2)


if __name__ == "__main__":
    main()
