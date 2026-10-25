package ru.secureoverlay.secure_overlay.tunnel.rpc;

import java.util.UUID;

public record TunnelAck (

        UUID tunnelId,
        boolean delivered,
        String reason
){

}
