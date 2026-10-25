#!/usr/bin/env bash

set -euo pipefail


echo
echo "====================================="
echo " Secure Overlay Acceptance"
echo "====================================="
echo


echo "[1/5] Clean regression suite"

./gradlew clean test


echo
echo "[2/5] DHT scaling experiment"

./gradlew runExperiment


echo
echo "[3/5] Tunnel experiment"

./gradlew runTunnelExperiment


echo
echo "[4/5] Plot generation"

if [ -x ".venv/bin/python" ]; then

    .venv/bin/python \
        scripts/plot_experiments.py

else

    python3 \
        scripts/plot_experiments.py

fi


echo
echo "[5/5] Validate artifacts"

test -s \
    experiments/lookup-results.csv

test -s \
    experiments/tunnel-results.csv

test -s \
    experiments/plots/lookup-latency.png

test -s \
    experiments/plots/lookup-success-rate.png

test -s \
    experiments/plots/tunnel-build-time.png

test -s \
    experiments/plots/tunnel-recovery.png


echo
echo "====================================="
echo " ACCEPTANCE SUCCESSFUL"
echo "====================================="