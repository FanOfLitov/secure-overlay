package ru.secureoverlay.secure_overlay.dht;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindNodeServiceTest {

    @Test
    void shouldReturnClosestContacts() {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );

        table.addOrRefresh(contact(1));
        table.addOrRefresh(contact(4));
        table.addOrRefresh(contact(8));
        table.addOrRefresh(contact(16));

        FindNodeService service =
                new FindNodeService(
                        table,
                        3
                );

        FindNodeResponse response =
                service.find(
                        nodeId(6)
                );

        assertEquals(
                3,
                response.contacts().size()
        );

        assertEquals(
                nodeId(4).toHex(),
                response.contacts()
                        .get(0)
                        .nodeId()
        );

        assertEquals(
                nodeId(1).toHex(),
                response.contacts()
                        .get(1)
                        .nodeId()
        );

        assertEquals(
                nodeId(8).toHex(),
                response.contacts()
                        .get(2)
                        .nodeId()
        );
    }


    private static Contact contact(
            int value
    ) {

        return new Contact(
                nodeId(value),
                "127.0.0.1",
                9000 + value,
                Instant.now()
        );
    }


    private static NodeId nodeId(
            int value
    ) {

        byte[] bytes =
                new byte[NodeId.LENGTH_BYTES];

        bytes[31] =
                (byte) value;

        return new NodeId(bytes);
    }
}