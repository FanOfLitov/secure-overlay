package ru.secureoverlay.secure_overlay.tunnel;

public record ResilientSendResult(
        boolean delivered,
        boolean recovered,
        long recoveryTimeMs,
        long endToEndLatencyMs,
        String reason
) {
}