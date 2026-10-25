package ru.secureoverlay.secure_overlay.dht.rpc;

public record StoreResponse(
        boolean stored,
        String reason
) {
}