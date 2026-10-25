import csv
import statistics
from pathlib import Path
import math
import matplotlib.pyplot as plt


ROOT = Path(__file__).resolve().parent.parent

LOOKUP_FILE = ROOT / "experiments" / "lookup-results.csv"
TUNNEL_FILE = ROOT / "experiments" / "tunnel-results.csv"

PLOTS_DIR = ROOT / "experiments" / "plots"

PLOTS_DIR.mkdir(parents=True, exist_ok=True)


def read_csv(path):
    with path.open(encoding="utf-8") as file:
        return list(csv.DictReader(file))


def percentile(values, p):
    values = sorted(values)

    if not values:
        return 0

    index = math.ceil(
        p * len(values)
    ) - 1

    return values[index]


def print_stats(name, values):
    if not values:
        return

    print()
    print(name)
    print("-" * len(name))

    print(f"N      = {len(values)}")
    print(f"mean   = {statistics.mean(values):.2f}")
    print(f"median = {statistics.median(values):.2f}")
    print(f"min    = {min(values)}")
    print(f"max    = {max(values)}")

    if len(values) >= 20:
        print(f"p95    = {percentile(values, 0.95)}")


def lookup_plots():
    rows = read_csv(LOOKUP_FILE)

    groups = {}

    for row in rows:
        key = (
            row["topology"],
            row["scenario"]
        )

        groups.setdefault(
            key,
            []
        ).append(row)

    labels = []
    avg_latency = []
    avg_rpc = []
    avg_iterations = []
    success_rate = []

    for key, group in sorted(groups.items()):

        topology, scenario = key

        label = f"{topology}\n{scenario}"

        labels.append(label)

        latency = [
            int(row["latency_ms"])
            for row in group
        ]

        rpc = [
            int(row["rpc_count"])
            for row in group
        ]

        iterations = [
            int(row["iterations"])
            for row in group
        ]

        successes = sum(
            row["success"].lower() == "true"
            for row in group
        )

        avg_latency.append(
            statistics.mean(latency)
        )

        avg_rpc.append(
            statistics.mean(rpc)
        )

        avg_iterations.append(
            statistics.mean(iterations)
        )

        success_rate.append(
            successes
            / len(group)
            * 100
        )

        print_stats(
            f"{topology} / {scenario} latency",
            latency
        )

    plt.figure(figsize=(9, 5))

    plt.bar(
        labels,
        avg_latency
    )

    plt.ylabel(
        "Average lookup latency, ms"
    )

    plt.title(
        "DHT lookup latency"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "lookup-latency.png",
        dpi=160
    )

    plt.close()


    plt.figure(figsize=(9, 5))

    plt.bar(
        labels,
        avg_rpc
    )

    plt.ylabel(
        "Average RPC count"
    )

    plt.title(
        "RPC cost per lookup"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "lookup-rpc.png",
        dpi=160
    )

    plt.close()


    plt.figure(figsize=(9, 5))

    plt.bar(
        labels,
        avg_iterations
    )

    plt.ylabel(
        "Average iterations"
    )

    plt.title(
        "Lookup iterations"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "lookup-iterations.png",
        dpi=160
    )

    plt.close()


    plt.figure(figsize=(9, 5))

    plt.bar(
        labels,
        success_rate
    )

    plt.ylim(
        0,
        105
    )

    plt.ylabel(
        "Successful lookups, %"
    )

    plt.title(
        "DHT lookup success rate"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "lookup-success-rate.png",
        dpi=160
    )

    plt.close()


def tunnel_plots():
    rows = read_csv(
        TUNNEL_FILE
    )

    normal = [
        row
        for row in rows
        if row["scenario"]
        == "normal"
    ]

    failures = [
        row
        for row in rows
        if row["scenario"]
        == "relay-failure"
    ]


    build_times = [
        int(row["build_time_ms"])
        for row in rows
    ]

    normal_latency = [
        int(row["end_to_end_latency_ms"])
        for row in normal
    ]

    recovery_times = [
        int(row["recovery_time_ms"])
        for row in failures
    ]


    print_stats(
        "Tunnel build time",
        build_times
    )

    print_stats(
        "Tunnel E2E latency",
        normal_latency
    )

    print_stats(
        "Tunnel recovery time",
        recovery_times
    )


    plt.figure(figsize=(9, 5))

    plt.plot(
        range(
            1,
            len(build_times) + 1
        ),
        build_times,
        marker="o"
    )

    plt.xlabel(
        "Observation"
    )

    plt.ylabel(
        "Build time, ms"
    )

    plt.title(
        "Tunnel build time"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "tunnel-build-time.png",
        dpi=160
    )

    plt.close()


    plt.figure(figsize=(9, 5))

    plt.plot(
        range(
            1,
            len(normal_latency) + 1
        ),
        normal_latency,
        marker="o"
    )

    plt.xlabel(
        "Message"
    )

    plt.ylabel(
        "End-to-end latency, ms"
    )

    plt.title(
        "Message latency through active tunnel"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "tunnel-e2e-latency.png",
        dpi=160
    )

    plt.close()


    plt.figure(figsize=(8, 5))

    plt.bar(
        [
            str(i)
            for i in range(
                1,
                len(recovery_times) + 1
            )
        ],
        recovery_times
    )

    plt.xlabel(
        "Relay failure injection"
    )

    plt.ylabel(
        "Recovery time, ms"
    )

    plt.title(
        "Tunnel recovery after relay failure"
    )

    plt.tight_layout()

    plt.savefig(
        PLOTS_DIR
        / "tunnel-recovery.png",
        dpi=160
    )

    plt.close()


def main():
    if not LOOKUP_FILE.exists():
        raise FileNotFoundError(
            LOOKUP_FILE
        )

    if not TUNNEL_FILE.exists():
        raise FileNotFoundError(
            TUNNEL_FILE
        )

    lookup_plots()

    tunnel_plots()

    print()
    print(
        "Plots written to:",
        PLOTS_DIR
    )


if __name__ == "__main__":
    main()