package ru.secureoverlay.secure_overlay.tunnel;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.TcpServer;
import ru.secureoverlay.secure_overlay.transport.TunnelRpcClient;
import ru.secureoverlay.secure_overlay.tunnel.rpc.TunnelAck;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiHopTunnelIntegrationTest {

    @Test
    void shouldDeliverEncryptedMessageThroughTwoRelays()
            throws Exception {

        int relay1Port = 9911;
        int relay2Port = 9912;
        int destinationPort = 9913;


        Contact relay1 =
                contact(
                        nodeId(11),
                        relay1Port
                );

        Contact relay2 =
                contact(
                        nodeId(12),
                        relay2Port
                );

        Contact destination =
                contact(
                        nodeId(13),
                        destinationPort
                );


        TcpServer relay1Server =
                new TcpServer(
                        relay1Port,
                        new RoutingTable(
                                relay1.nodeId(),
                                3
                        )
                );


        TcpServer relay2Server =
                new TcpServer(
                        relay2Port,
                        new RoutingTable(
                                relay2.nodeId(),
                                3
                        )
                );


        TcpServer destinationServer =
                new TcpServer(
                        destinationPort,
                        new RoutingTable(
                                destination.nodeId(),
                                3
                        )
                );


        Thread relay1Thread =
                new Thread(
                        relay1Server::start
                );

        Thread relay2Thread =
                new Thread(
                        relay2Server::start
                );

        Thread destinationThread =
                new Thread(
                        destinationServer::start
                );


        relay1Thread.start();
        relay2Thread.start();
        destinationThread.start();


        Thread.sleep(500);


        try {

            TunnelClient client =
                    new TunnelClient(
                            new TunnelRpcClient()
                    );


            /*
             * route:
             *
             * initiator
             *    ↓
             * relay1
             *    ↓
             * relay2
             *    ↓
             * destination
             */
            BuiltTunnel tunnel =
                    client.build(
                            List.of(
                                    relay1,
                                    relay2,
                                    destination
                            )
                    );


            assertNotNull(
                    tunnel
            );


            String message =
                    "SECRET THROUGH TWO RELAYS";


            TunnelAck ack =
                    client.send(
                            tunnel,
                            message.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            assertTrue(
                    ack.delivered()
            );


            byte[] received =
                    destinationServer
                            .getTunnelStateStore()
                            .getReceivedMessage(
                                    tunnel.tunnelId()
                            );


            assertNotNull(
                    received
            );


            assertEquals(
                    message,
                    new String(
                            received,
                            StandardCharsets.UTF_8
                    )
            );


            /*
             * Relay'и НЕ имеют endpoint AES key.
             */
            assertNull(
                    relay1Server
                            .getTunnelStateStore()
                            .getEndpointKey(
                                    tunnel.tunnelId()
                            )
            );


            assertNull(
                    relay2Server
                            .getTunnelStateStore()
                            .getEndpointKey(
                                    tunnel.tunnelId()
                            )
            );


            /*
             * Destination имеет AES key.
             */
            assertNotNull(
                    destinationServer
                            .getTunnelStateStore()
                            .getEndpointKey(
                                    tunnel.tunnelId()
                            )
            );


        } finally {

            relay1Server.stop();
            relay2Server.stop();
            destinationServer.stop();

            relay1Thread.join(2000);
            relay2Thread.join(2000);
            destinationThread.join(2000);
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