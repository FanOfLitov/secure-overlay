package ru.secureoverlay.secure_overlay.dht;

import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.time.Instant;

public record Contact(
        NodeId nodeId,
        String host,
        int port,
        Instant lastSeen
) {

    public Contact {

        if (nodeId == null) {
            throw new IllegalArgumentException(
                    "NodeId cannot be null"
            );
        }

        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(
                    "Host cannot be empty"
            );
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Invalid port: " + port
            );
        }

        if (lastSeen == null) {
            throw new IllegalArgumentException(
                    "lastSeen cannot be null"
            );
        }
    }

    public Contact refreshed() {

        return new Contact(
                nodeId,
                host,
                port,
                Instant.now()
        );
    }
}