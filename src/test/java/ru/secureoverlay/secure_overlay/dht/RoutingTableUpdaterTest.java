package ru.secureoverlay.secure_overlay.dht;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreResponse;
import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpc;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutingTableUpdaterTest {

    @Test
    void shouldKeepLiveLruContact() {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );


        table.addOrRefresh(contact(4));
        table.addOrRefresh(contact(5));
        table.addOrRefresh(contact(6));

        DhtRpc rpc =
                new FakeRpc(true);

        RoutingTableUpdater updater =
                new RoutingTableUpdater(
                        table,
                        rpc
                );

        boolean added =
                updater.addContact(
                        contact(7)
                );


        assertFalse(added);

        assertTrue(
                table.getBucket(2)
                        .contains(
                                nodeId(4)
                        )
        );

        assertFalse(
                table.getBucket(2)
                        .contains(
                                nodeId(7)
                        )
        );
    }


    @Test
    void shouldReplaceDeadLruContact() {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );

        table.addOrRefresh(contact(4));
        table.addOrRefresh(contact(5));
        table.addOrRefresh(contact(6));

        DhtRpc rpc =
                new FakeRpc(false);

        RoutingTableUpdater updater =
                new RoutingTableUpdater(
                        table,
                        rpc
                );

        boolean added =
                updater.addContact(
                        contact(7)
                );

        /*
         * LRU-контакт мёртв,
         * поэтому заменяем его новым.
         */
        assertTrue(added);

        assertFalse(
                table.getBucket(2)
                        .contains(
                                nodeId(4)
                        )
        );

        assertTrue(
                table.getBucket(2)
                        .contains(
                                nodeId(7)
                        )
        );
    }


    private static class FakeRpc
            implements DhtRpc {

        private final boolean alive;


        private FakeRpc(
                boolean alive
        ) {
            this.alive = alive;
        }


        @Override
        public boolean ping(
                String host,
                int port
        ) {
            return alive;
        }


        @Override
        public FindNodeResponse findNode(
                String host,
                int port,
                NodeId target
        ) {

            return new FindNodeResponse(
                    List.of()
            );
        }


        @Override
        public StoreResponse store(
                String host,
                int port,
                DhtRecord record
        ) {

            throw new UnsupportedOperationException(
                    "STORE is not used in this test"
            );
        }


        @Override
        public FindValueResponse findValue(
                String host,
                int port,
                String key
        ) {

            throw new UnsupportedOperationException(
                    "FIND_VALUE is not used in this test"
            );
        }
    }


    private static Contact contact(
            int value
    ) {

        return new Contact(
                nodeId(value),
                "127.0.0.1",
                9400 + value,
                Instant.now()
        );
    }


    private static NodeId nodeId(
            int value
    ) {

        byte[] bytes =
                new byte[
                        NodeId.LENGTH_BYTES
                        ];

        bytes[31] =
                (byte) value;

        return new NodeId(bytes);
    }
}