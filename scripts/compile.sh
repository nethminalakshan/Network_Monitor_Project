#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/backend/src/main/java"
OUT="$ROOT/backend/out"

echo "========================================"
echo "  Network Monitor - Compilation Script"
echo "========================================"
echo ""

rm -rf "$OUT"
mkdir -p "$OUT"

cd "$SRC"
echo "Compiling Java sources..."
javac -d "$OUT" -cp "$OUT" \
  com/networkmonitor/model/Device.java \
  com/networkmonitor/discovery/DeviceDiscovery.java \
  com/networkmonitor/monitor/*.java \
  com/networkmonitor/api/*.java \
  com/networkmonitor/NetworkMonitor.java

echo ""
echo "========================================"
echo "  Backend compiled to backend/out"
echo "========================================"
echo ""
