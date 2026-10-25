package ru.secureoverlay.secure_overlay.tunnel;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.transport.TunnelRpcClient;
import ru.secureoverlay.secure_overlay.tunnel.rpc.*;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

public class TunnelClient {

    private final TunnelRpcClient rpc;


    public TunnelClient(TunnelRpcClient rpc
    ) {

        this.rpc = rpc;
    }


    public BuiltTunnel build(List<Contact> route) throws Exception {

        /*
         * Минимум:
         *
         * relay1
         * relay2
         * destination
         *
         * => два relay.
         */
        if (route == null || route.size() < 3) {

            throw new IllegalArgumentException("Tunnel requires at least 2 relays and destination");
        }


        UUID tunnelId = UUID.randomUUID();


        KeyPair initiatorEphemeral = TunnelCrypto.generateEphemeralKeyPair();


        Contact entry = route.get(0);


        List<Contact> remaining = route.subList(1, route.size()
                );


        TunnelBuildRequest request = new TunnelBuildRequest(tunnelId,

                        TunnelCrypto.encodePublicKey(
                                initiatorEphemeral
                                        .getPublic()
                        ),

                        remaining.stream()
                                .map(
                                        ContactMapper::toDto
                                )
                                .toList()
                );


        TunnelBuildResponse response = rpc.build(
                        entry.host(),
                        entry.port(),
                        request
                );


        if (!response.built()) {throw new IllegalStateException(
                    "Tunnel build failed: "
                            + response.reason()
            );
        }


        PublicKey destinationPublic =
                TunnelCrypto.decodePublicKey(
                        response
                                .destinationEphemeralPublicKey()
                );


        SecretKey key =
                TunnelCrypto.deriveKey(
                        initiatorEphemeral,
                        destinationPublic,
                        tunnelId
                );


        return new BuiltTunnel(
                tunnelId,
                entry,
                key
        );
    }


    public TunnelAck send(
            BuiltTunnel tunnel,
            byte[] plaintext
    ) throws Exception {

        EncryptedTunnelPayload encrypted =
                TunnelCrypto.encrypt(
                        tunnel.key(),
                        tunnel.tunnelId(),
                        plaintext
                );


        TunnelDataRequest request =
                new TunnelDataRequest(
                        tunnel.tunnelId(),
                        encrypted
                );


        return rpc.sendData(
                tunnel.entryRelay()
                        .host(),

                tunnel.entryRelay()
                        .port(),

                request
        );
    }
}