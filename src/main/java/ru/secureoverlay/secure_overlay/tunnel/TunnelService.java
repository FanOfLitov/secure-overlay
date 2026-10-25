package ru.secureoverlay.secure_overlay.tunnel;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactDto;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.transport.TunnelRpcClient;
import ru.secureoverlay.secure_overlay.tunnel.rpc.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TunnelService {

    private final TunnelRpcClient rpcClient;

    private final TunnelStateStore stateStore;


    public TunnelService(TunnelRpcClient rpcClient, TunnelStateStore stateStore
    ) {

        this.rpcClient = rpcClient;

        this.stateStore = stateStore;
    }


    public TunnelBuildResponse handleBuild(TunnelBuildRequest request) {

        /*
         * remainingRoute пуст:
         * текущий узел = destination.
         */
        if (request.remainingRoute() == null || request.remainingRoute().isEmpty()) {

            return becomeDestination(request);
        }


        /*
         * Текущий узел = relay.
         *
         * Первый элемент remainingRoute
         * становится next hop.
         */
        ContactDto nextDto =
                request.remainingRoute()
                        .get(0);


        Contact next =
                ContactMapper.fromDto(
                        nextDto
                );


        stateStore.putNextHop(
                request.tunnelId(),
                next
        );


        List<ContactDto> remaining =
                new ArrayList<>(
                        request.remainingRoute()
                );


        remaining.remove(0);


        TunnelBuildRequest forwarded =
                new TunnelBuildRequest(
                        request.tunnelId(),
                        request
                                .initiatorEphemeralPublicKey(),
                        remaining
                );


        try {

            return rpcClient.build(
                    next.host(),
                    next.port(),
                    forwarded
            );


        } catch (Exception e) {

            stateStore.remove(
                    request.tunnelId()
            );


            return new TunnelBuildResponse(
                    request.tunnelId(),
                    false,
                    null,
                    "NEXT_HOP_FAILED: "
                            + e.getMessage()
            );
        }
    }


    private TunnelBuildResponse becomeDestination(
            TunnelBuildRequest request
    ) {

        KeyPair destinationEphemeral =
                TunnelCrypto
                        .generateEphemeralKeyPair();


        PublicKey initiatorPublic =
                TunnelCrypto
                        .decodePublicKey(
                                request
                                        .initiatorEphemeralPublicKey()
                        );


        SecretKey tunnelKey =
                TunnelCrypto.deriveKey(
                        destinationEphemeral,
                        initiatorPublic,
                        request.tunnelId()
                );


        stateStore.putEndpointKey(
                request.tunnelId(),
                tunnelKey
        );


        return new TunnelBuildResponse(
                request.tunnelId(),
                true,
                TunnelCrypto.encodePublicKey(
                        destinationEphemeral
                                .getPublic()
                ),
                "BUILT"
        );
    }


    public TunnelAck handleData(
            TunnelDataRequest request
    ) {

        /*
         * Relay?
         */
        Contact next =
                stateStore.getNextHop(
                        request.tunnelId()
                );


        if (next != null) {

            try {

                /*
                 * encryptedPayload вообще
                 * не расшифровываем.
                 */
                return rpcClient.sendData(
                        next.host(),
                        next.port(),
                        request
                );


            } catch (Exception e) {

                return new TunnelAck(
                        request.tunnelId(),
                        false,
                        "FORWARD_FAILED: "
                                + e.getMessage()
                );
            }
        }


        /*
         * Destination?
         */
        SecretKey key =
                stateStore.getEndpointKey(
                        request.tunnelId()
                );


        if (key == null) {

            return new TunnelAck(
                    request.tunnelId(),
                    false,
                    "UNKNOWN_TUNNEL"
            );
        }


        byte[] plaintext =
                TunnelCrypto.decrypt(
                        key,
                        request.tunnelId(),
                        request.encryptedPayload()
                );


        stateStore.storeReceivedMessage(
                request.tunnelId(),
                plaintext
        );


        System.out.println(
                "[TUNNEL] Destination received: "
                        + new String(
                        plaintext,
                        StandardCharsets.UTF_8
                )
        );


        return new TunnelAck(
                request.tunnelId(),
                true,
                "DELIVERED"
        );
    }


    public void close(
            UUID tunnelId
    ) {

        Contact next =
                stateStore.getNextHop(
                        tunnelId
                );


        if (next != null) {

            try {

                rpcClient.closeTunnel(
                        next.host(),
                        next.port(),
                        tunnelId
                );

            } catch (Exception ignored) {
            }
        }


        stateStore.remove(
                tunnelId
        );
    }
}