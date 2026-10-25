package ru.secureoverlay.secure_overlay.dht.storage;

public record DhtRecord(

        String key,

        String value,

        long version,

        long expiresAtEpochMs,

        String ownerPublicKey,

        String signature

) {

    public DhtRecord {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Key cannot be blank"
            );
        }

        if (value == null) {
            throw new IllegalArgumentException(
                    "Value cannot be null"
            );
        }

        if (version < 0) {
            throw new IllegalArgumentException(
                    "Version cannot be negative"
            );
        }

        if (ownerPublicKey == null
                || ownerPublicKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Owner public key cannot be blank"
            );
        }

        if (signature == null
                || signature.isBlank()) {

            throw new IllegalArgumentException(
                    "Signature cannot be blank"
            );
        }
    }

    public boolean expired() {

        return System.currentTimeMillis()
                >= expiresAtEpochMs;
    }
}