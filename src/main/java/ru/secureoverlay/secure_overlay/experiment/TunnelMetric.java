package ru.secureoverlay.secure_overlay.experiment;

public record TunnelMetric(

        int run,

        String scenario,

        boolean delivered,

        long buildTimeMs,

        long endToEndLatencyMs,

        long recoveryTimeMs

) {
}