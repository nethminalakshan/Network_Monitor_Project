#!/bin/bash
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/backend/out"

echo "========================================"
echo "  Network Monitor - Launch Script"
echo "========================================"
echo ""

if [ ! -f "$OUT/com/networkmonitor/NetworkMonitor.class" ]; then
    echo "Compiled files not found. Compiling first..."
    "$ROOT/compile.sh"
fi

echo "Starting Network Monitor..."
echo ""
echo "----------------------------------------"
echo "  IMPORTANT NOTES:"
echo "----------------------------------------"
echo "1. May require sudo for full features"
echo "2. Ensure firewall allows ICMP and ARP"
echo "3. Press Ctrl+C to stop monitoring"
echo "----------------------------------------"
echo ""

cd "$ROOT/backend"

if [ "$EUID" -ne 0 ]; then
    echo "Warning: Not running as root."
    echo "Some features may not work properly."
    echo "Consider running with: sudo ./run.sh"
    echo ""
fi

java -cp "$OUT" com.networkmonitor.NetworkMonitor
