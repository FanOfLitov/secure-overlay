package ru.secureoverlay.secure_overlay.experiment;

import java.util.List;

public final class ExperimentSummary {

    private ExperimentSummary() {
    }


    public static void print(
            List<LookupMetric> metrics
    ) {

        if (metrics.isEmpty()) {

            System.out.println(
                    "No metrics"
            );

            return;
        }


        long successful =
                metrics.stream()
                        .filter(
                                LookupMetric::success
                        )
                        .count();


        double successRate =
                successful * 100.0
                        / metrics.size();


        double avgLatency =
                metrics.stream()
                        .mapToLong(
                                LookupMetric::latencyMs
                        )
                        .average()
                        .orElse(0);


        double avgRpc =
                metrics.stream()
                        .mapToInt(
                                LookupMetric::rpcCount
                        )
                        .average()
                        .orElse(0);


        double avgIterations =
                metrics.stream()
                        .mapToInt(
                                LookupMetric::iterations
                        )
                        .average()
                        .orElse(0);


        System.out.println(
                "-----------------------------"
        );

        System.out.println(
                "Lookups: "
                        + metrics.size()
        );

        System.out.printf(
                "Success rate: %.2f%%%n",
                successRate
        );

        System.out.printf(
                "Average latency: %.2f ms%n",
                avgLatency
        );

        System.out.printf(
                "Average RPC count: %.2f%n",
                avgRpc
        );

        System.out.printf(
                "Average iterations: %.2f%n",
                avgIterations
        );

        System.out.println(
                "-----------------------------"
        );
    }
}