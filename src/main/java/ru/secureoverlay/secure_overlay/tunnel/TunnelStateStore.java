package ru.secureoverlay.secure_overlay.tunnel;

import ru.secureoverlay.secure_overlay.dht.Contact;

import javax.crypto.SecretKey;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TunnelStateStore {

    /*
     * Для relay:
     * tunnelId -> next hop
     */
    private final ConcurrentHashMap<UUID, Contact>
            nextHops =
            new ConcurrentHashMap<>();


    /*
     * Для destination:
     * tunnelId -> end-to-end AES key
     */
    private final ConcurrentHashMap<UUID, SecretKey>
            endpointKeys =
            new ConcurrentHashMap<>();


    private final ConcurrentHashMap<UUID, byte[]>
            receivedMessages =
            new ConcurrentHashMap<>();


    public void putNextHop(
            UUID tunnelId,
            Contact contact
    ) {

        nextHops.put(
                tunnelId,
                contact
        );
    }


    public Contact getNextHop(
            UUID tunnelId
    ) {

        return nextHops.get(
                tunnelId
        );
    }


    public void putEndpointKey(
            UUID tunnelId,
            SecretKey key
    ) {

        endpointKeys.put(
                tunnelId,
                key
        );
    }


    public SecretKey getEndpointKey(
            UUID tunnelId
    ) {

        return endpointKeys.get(
                tunnelId
        );
    }


    public void storeReceivedMessage(
            UUID tunnelId,
            byte[] message
    ) {

        receivedMessages.put(
                tunnelId,
                message.clone()
        );
    }


    public byte[] getReceivedMessage(
            UUID tunnelId
    ) {

        byte[] value =
                receivedMessages.get(
                        tunnelId
                );

        if (value == null) {
            return null;
        }

        return value.clone();
    }


    public void remove(
            UUID tunnelId
    ) {

        nextHops.remove(
                tunnelId
        );

        endpointKeys.remove(
                tunnelId
        );

        receivedMessages.remove(
                tunnelId
        );
    }
}