package ru.secureoverlay.secure_overlay.tunnel;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.TcpServer;
import ru.secureoverlay.secure_overlay.transport.TunnelRpcClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TunnelRecoveryIntegrationTest {

    @Test
    void shouldAutomaticallyRebuildTunnelAfterRelayFailure()
            throws Exception {

        Contact relay1 =
                contact(
                        nodeId(21),
                        9921
                );

        Contact relay2 =
                contact(
                        nodeId(22),
                        9922
                );

        Contact relay3 =
                contact(
                        nodeId(23),
                        9923
                );

        Contact relay4 =
                contact(
                        nodeId(24),
                        9924
                );

        Contact destination =
                contact(
                        nodeId(25),
                        9925
                );


        TcpServer relay1Server =
                server(relay1);

        TcpServer relay2Server =
                server(relay2);

        TcpServer relay3Server =
                server(relay3);

        TcpServer relay4Server =
                server(relay4);

        TcpServer destinationServer =
                server(destination);


        Thread t1 =
                start(relay1Server);

        Thread t2 =
                start(relay2Server);

        Thread t3 =
                start(relay3Server);

        Thread t4 =
                start(relay4Server);

        Thread td =
                start(destinationServer);


        Thread.sleep(500);


        try {

            TunnelClient baseClient =
                    new TunnelClient(
                            new TunnelRpcClient()
                    );


            List<Contact> primary =
                    List.of(
                            relay1,
                            relay2,
                            destination
                    );


            List<Contact> backup =
                    List.of(
                            relay3,
                            relay4,
                            destination
                    );


            ResilientTunnelClient client =
                    new ResilientTunnelClient(
                            baseClient,
                            primary,
                            backup
                    );


            TunnelBuildMetric buildMetric =
                    client.connect();


            assertNotNull(
                    buildMetric.tunnel()
            );


            System.out.println(
                    "[RECOVERY] initial build="
                            + buildMetric.buildTimeMs()
                            + " ms"
            );


            ResilientSendResult first =
                    client.send(
                            "BEFORE-FAILURE"
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );


            assertTrue(
                    first.delivered()
            );

            assertFalse(
                    first.recovered()
            );


            /*
             * Ломаем активный relay.
             */
            relay2Server.stop();


            Thread.sleep(200);


            ResilientSendResult recovered =
                    client.send(
                            "AFTER-FAILURE"
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );


            assertTrue(
                    recovered.delivered()
            );

            assertTrue(
                    recovered.recovered()
            );


            System.out.println(
                    "[RECOVERY] recovery="
                            + recovered.recoveryTimeMs()
                            + " ms"
            );


            byte[] destinationMessage =
                    destinationServer
                            .getTunnelStateStore()
                            .getReceivedMessage(
                                    client.activeTunnel()
                                            .tunnelId()
                            );


            assertNotNull(
                    destinationMessage
            );


            assertEquals(
                    "AFTER-FAILURE",

                    new String(
                            destinationMessage,
                            StandardCharsets.UTF_8
                    )
            );


        } finally {

            relay1Server.stop();
            relay2Server.stop();
            relay3Server.stop();
            relay4Server.stop();
            destinationServer.stop();

            t1.join(2000);
            t2.join(2000);
            t3.join(2000);
            t4.join(2000);
            td.join(2000);
        }
    }


    private static TcpServer server(
            Contact contact
    ) {

        return new TcpServer(
                contact.port(),
                new RoutingTable(
                        contact.nodeId(),
                        3
                )
        );
    }


    private static Thread start(
            TcpServer server
    ) {

        Thread thread =
                new Thread(
                        server::start
                );

        thread.start();

        return thread;
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