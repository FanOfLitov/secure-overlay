package ru.secureoverlay.secure_overlay.tunnel;

public record TunnelBuildMetric(
        BuiltTunnel tunnel,
        long buildTimeMs
) {
}