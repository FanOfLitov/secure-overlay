package ru.secureoverlay.secure_overlay.dht.storage;

public record StoreResult(
        boolean stored,
        String reason
) {
}