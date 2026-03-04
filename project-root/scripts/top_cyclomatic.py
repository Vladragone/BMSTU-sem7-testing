#!/usr/bin/env python3
import argparse
import csv
import io
import subprocess
import sys
from pathlib import Path


def run_lizard(source_dir: str, exclude_pattern: str) -> list[list[str]]:
    cmd = [
        sys.executable,
        "-m",
        "lizard",
        source_dir,
        "-l",
        "java",
        "-C",
        "0",
        "--csv",
        "-x",
        exclude_pattern,
    ]
    try:
        output = subprocess.check_output(cmd, text=True, stderr=subprocess.STDOUT)
    except subprocess.CalledProcessError as exc:
        print("Failed to run lizard.\nOutput:\n" + exc.output, file=sys.stderr)
        sys.exit(1)
    except FileNotFoundError:
        print("Python runtime not found for subprocess execution.", file=sys.stderr)
        sys.exit(1)

    rows = list(csv.reader(io.StringIO(output)))
    if not rows:
        return []
    return rows[1:] if rows[0] and rows[0][0].lower() == "nloc" else rows


def normalize_path(path_value: str) -> str:
    return path_value.replace("\\", "/")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Show TOP methods by cyclomatic complexity (CCN) using lizard."
    )
    parser.add_argument(
        "--source",
        default="project-root/src/main/java",
        help="Source directory to analyze (default: project-root/src/main/java)",
    )
    parser.add_argument(
        "--exclude",
        default="*/quality/*",
        help='Lizard exclude pattern (default: "*/quality/*")',
    )
    parser.add_argument(
        "--top",
        type=int,
        default=10,
        help="How many methods to show (default: 10)",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    source_path = Path(args.source)
    if not source_path.exists():
        print(f"Source path not found: {source_path}", file=sys.stderr)
        sys.exit(1)

    rows = run_lizard(str(source_path), args.exclude)
    rows = [row for row in rows if len(row) >= 11]
    rows.sort(key=lambda row: int(row[1]), reverse=True)

    if not rows:
        print("No functions found.")
        return

    print(f"TOP {args.top} by cyclomatic complexity (CCN):")
    for row in rows[: args.top]:
        ccn = row[1]
        long_name = row[8]
        file_path = normalize_path(row[6])
        start_line = row[9]
        end_line = row[10]
        print(f"CCN={ccn:>2} | {long_name} | {file_path}:{start_line}-{end_line}")


if __name__ == "__main__":
    main()
