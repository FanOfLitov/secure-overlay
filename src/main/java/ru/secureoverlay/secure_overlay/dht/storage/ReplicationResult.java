package ru.secureoverlay.secure_overlay.dht.storage;

public record ReplicationResult(
        int attempted,
        int successful,
        int required
) {

    public boolean isSuccessful() {

        return successful >= required;
    }
}