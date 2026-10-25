package ru.secureoverlay.secure_overlay.experiment;

public record LookupMetric(

        String topology,

        String scenario,

        int run,

        String sourceNodeId,

        String targetNodeId,

        boolean success,

        long latencyMs,

        int rpcCount,

        int iterations

) {
}