package ru.secureoverlay.secure_overlay.tunnel.rpc;

import java.util.UUID;

public record TunnelBuildResponse(

        UUID tunnelId,

        boolean built,

        String destinationEphemeralPublicKey,

        String reason

) {
}