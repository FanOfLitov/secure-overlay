package ru.secureoverlay.secure_overlay.dht.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.lookup.IterativeNodeLookup;
import ru.secureoverlay.secure_overlay.identity.IdentityService;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.identity.NodeIdentity;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReplicationFailureIntegrationTest {

    @TempDir
    Path tempDir;


    @Test
    void shouldFindValueAfterOneReplicaFails()
            throws Exception {

        int port1 = 9811;
        int port2 = 9812;
        int port3 = 9813;
        int port4 = 9814;


        NodeId node1 =
                nodeId(10);

        NodeId node2 =
                nodeId(20);

        NodeId node3 =
                nodeId(30);

        NodeId node4 =
                nodeId(40);


        RoutingTable table1 =
                new RoutingTable(
                        node1,
                        3
                );

        RoutingTable table2 =
                new RoutingTable(
                        node2,
                        3
                );

        RoutingTable table3 =
                new RoutingTable(
                        node3,
                        3
                );

        RoutingTable table4 =
                new RoutingTable(
                        node4,
                        3
                );


        /*
         * Каждый сервер знает остальные.
         */
        connect(
                table1,
                List.of(
                        contact(node2, port2),
                        contact(node3, port3),
                        contact(node4, port4)
                )
        );

        connect(
                table2,
                List.of(
                        contact(node1, port1),
                        contact(node3, port3),
                        contact(node4, port4)
                )
        );

        connect(
                table3,
                List.of(
                        contact(node1, port1),
                        contact(node2, port2),
                        contact(node4, port4)
                )
        );

        connect(
                table4,
                List.of(
                        contact(node1, port1),
                        contact(node2, port2),
                        contact(node3, port3)
                )
        );


        TcpServer server1 =
                new TcpServer(
                        port1,
                        table1
                );

        TcpServer server2 =
                new TcpServer(
                        port2,
                        table2
                );

        TcpServer server3 =
                new TcpServer(
                        port3,
                        table3
                );

        TcpServer server4 =
                new TcpServer(
                        port4,
                        table4
                );


        Thread thread1 =
                new Thread(
                        server1::start
                );

        Thread thread2 =
                new Thread(
                        server2::start
                );

        Thread thread3 =
                new Thread(
                        server3::start
                );

        Thread thread4 =
                new Thread(
                        server4::start
                );


        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();


        Thread.sleep(500);


        try {

            /*
             * Клиентская DHT routing table.
             */
            RoutingTable clientTable =
                    new RoutingTable(
                            nodeId(1),
                            4
                    );


            clientTable.addOrRefresh(
                    contact(
                            node1,
                            port1
                    )
            );

            clientTable.addOrRefresh(
                    contact(
                            node2,
                            port2
                    )
            );

            clientTable.addOrRefresh(
                    contact(
                            node3,
                            port3
                    )
            );

            clientTable.addOrRefresh(
                    contact(
                            node4,
                            port4
                    )
            );


            DhtRpcClient rpc =
                    new DhtRpcClient();


            IterativeNodeLookup lookup =
                    new IterativeNodeLookup(
                            clientTable,
                            rpc,
                            3,
                            4
                    );


            NodeIdentity identity =
                    new IdentityService()
                            .loadOrCreate(
                                    tempDir.resolve(
                                            "owner"
                                    )
                            );


            DhtRecord record =
                    DhtRecordSigner.sign(
                            "replicated-key",
                            "SURVIVES-FAILURE",
                            1,
                            System.currentTimeMillis()
                                    + 60_000,
                            identity
                    );


            ReplicationService replication =
                    new ReplicationService(
                            lookup,
                            rpc,
                            3
                    );


            ReplicationResult replicationResult =
                    replication.replicate(
                            record
                    );


            assertEquals(
                    3,
                    replicationResult.attempted()
            );

            assertEquals(
                    3,
                    replicationResult.successful()
            );

            assertTrue(
                    replicationResult.successful()
                            >= 3
            );


            /*
             * Определяем, какие реальные
             * серверы получили копию.
             */
            List<TcpServer> holders =
                    List.of(
                                    server1,
                                    server2,
                                    server3,
                                    server4
                            )
                            .stream()

                            .filter(
                                    server ->
                                            server.getStore()
                                                    .get(
                                                            "replicated-key"
                                                    )
                                                    .isPresent()
                            )

                            .toList();


            assertEquals(
                    3,
                    holders.size()
            );


            /*
             * Убиваем одного владельца replica.
             */
            TcpServer failedHolder =
                    holders.get(0);


            failedHolder.stop();


            Thread.sleep(200);


            /*
             * Теперь ищем значение через DHT.
             *
             * Один из трёх holders уже мёртв.
             */
            FindValueService findValueService =
                    new FindValueService(
                            clientTable,
                            rpc,
                            3
                    );


            DhtRecord found =
                    findValueService.find(
                            "replicated-key"
                    );


            assertNotNull(
                    found
            );


            assertEquals(
                    "SURVIVES-FAILURE",
                    found.value()
            );


            assertTrue(
                    DhtRecordSigner.verify(
                            found
                    )
            );


            lookup.shutdown();


        } finally {

            server1.stop();
            server2.stop();
            server3.stop();
            server4.stop();

            thread1.join(2000);
            thread2.join(2000);
            thread3.join(2000);
            thread4.join(2000);
        }
    }


    private static void connect(
            RoutingTable table,
            List<Contact> contacts
    ) {

        for (Contact contact : contacts) {

            table.addOrRefresh(
                    contact
            );
        }
    }


    private static Contact contact(
            NodeId nodeId,
            int port
    ) {

        return new Contact(
                nodeId,
                "127.0.0.1",
                port,
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

        return new NodeId(
                bytes
        );
    }
}