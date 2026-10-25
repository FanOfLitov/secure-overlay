package ru.secureoverlay.secure_overlay.dht.rpc;

public record ContactDto(
        String nodeId,
        String host,
        int port
) {
}
