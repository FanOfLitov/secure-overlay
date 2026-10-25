package ru.secureoverlay.secure_overlay.dht;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutingTableTest {

    @Test
    void shouldCalculateCorrectBucketIndex() {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );

        assertEquals(
                0,
                table.bucketIndexFor(
                        nodeId(1)
                )
        );

        assertEquals(
                1,
                table.bucketIndexFor(
                        nodeId(2)
                )
        );

        assertEquals(
                1,
                table.bucketIndexFor(
                        nodeId(3)
                )
        );

        assertEquals(
                2,
                table.bucketIndexFor(
                        nodeId(4)
                )
        );

        assertEquals(
                2,
                table.bucketIndexFor(
                        nodeId(7)
                )
        );

        assertEquals(
                3,
                table.bucketIndexFor(
                        nodeId(8)
                )
        );
    }


    @Test
    void shouldRejectLocalNodeBucket() {

        NodeId local =
                nodeId(10);

        RoutingTable table =
                new RoutingTable(
                        local,
                        3
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> table.bucketIndexFor(local)
        );
    }


    @Test
    void shouldStoreContactsInDifferentBuckets() {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );

        assertEquals(
                0,
                table.bucketIndexFor(nodeId(1))
        );

        assertEquals(
                1,
                table.bucketIndexFor(nodeId(2))
        );

        assertEquals(
                2,
                table.bucketIndexFor(nodeId(4))
        );

        assertTrue(
                table.addOrRefresh(contact(1))
        );

        assertTrue(
                table.addOrRefresh(contact(2))
        );

        assertTrue(
                table.addOrRefresh(contact(4))
        );

        assertEquals(
                3,
                table.totalContacts()
        );

        assertEquals(
                1,
                table.getBucket(0).size()
        );

        assertEquals(
                1,
                table.getBucket(1).size()
        );

        assertEquals(
                1,
                table.getBucket(2).size()
        );
    }


    @Test
    void shouldFindClosestContacts() {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );

        table.addOrRefresh(contact(1));
        table.addOrRefresh(contact(4));
        table.addOrRefresh(contact(8));
        table.addOrRefresh(contact(16));

        NodeId target =
                nodeId(6);

        List<Contact> closest =
                table.findClosest(
                        target,
                        3
                );

        assertEquals(
                3,
                closest.size()
        );

        /*
         * target = 6
         *
         * distance:
         *
         * 4 XOR 6 = 2
         * 1 XOR 6 = 7
         * 8 XOR 6 = 14
         * 16 XOR 6 = 22
         */

        assertEquals(
                nodeId(4),
                closest.get(0).nodeId()
        );

        assertEquals(
                nodeId(1),
                closest.get(1).nodeId()
        );

        assertEquals(
                nodeId(8),
                closest.get(2).nodeId()
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