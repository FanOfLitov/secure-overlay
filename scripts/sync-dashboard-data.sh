#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

SOURCE_DIR="$ROOT_DIR/experiments"
TARGET_DIR="$ROOT_DIR/frontend/public/data"

mkdir -p "$TARGET_DIR"

cp \
  "$SOURCE_DIR/lookup-results.csv" \
  "$TARGET_DIR/lookup-results.csv"

cp \
  "$SOURCE_DIR/tunnel-results.csv" \
  "$TARGET_DIR/tunnel-results.csv"

echo "[DASHBOARD] Data synchronized"
echo "[DASHBOARD] $TARGET_DIR"