package ru.secureoverlay.secure_overlay.tunnel.rpc;

import ru.secureoverlay.secure_overlay.dht.rpc.ContactDto;

import java.util.List;
import java.util.UUID;

public record TunnelBuildRequest(

        UUID tunnelId,

        String initiatorEphemeralPublicKey,

        List<ContactDto> remainingRoute

) {
}