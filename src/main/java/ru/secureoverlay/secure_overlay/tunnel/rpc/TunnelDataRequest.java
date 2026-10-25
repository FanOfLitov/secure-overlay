package ru.secureoverlay.secure_overlay.tunnel.rpc;

import ru.secureoverlay.secure_overlay.tunnel.EncryptedTunnelPayload;

import java.util.UUID;

public record TunnelDataRequest(

        UUID tunnelId,

        EncryptedTunnelPayload encryptedPayload

) {
}