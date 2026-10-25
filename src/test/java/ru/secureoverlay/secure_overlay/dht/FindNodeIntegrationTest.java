package ru.secureoverlay.secure_overlay.dht;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FindNodeIntegrationTest {
    @Test
    void shouldFindClosestNodesOverTcp()
            throws Exception {

        int port = 9200;

        NodeId serverNodeId =
                nodeId(0);

        RoutingTable routingTable =
                new RoutingTable(
                        serverNodeId,
                        3
                );

        routingTable.addOrRefresh(
                contact(1)
        );

        routingTable.addOrRefresh(
                contact(4)
        );

        routingTable.addOrRefresh(
                contact(8)
        );

        routingTable.addOrRefresh(
                contact(16)
        );

        TcpServer server =
                new TcpServer(
                        port,
                        routingTable
                );

        Thread serverThread =
                new Thread(
                        server::start
                );

        serverThread.start();

        Thread.sleep(300);

        DhtRpcClient client =
                new DhtRpcClient();

        FindNodeResponse response =
                client.findNode(
                        "127.0.0.1",
                        port,
                        nodeId(6)
                );

        assertEquals(
                3,
                response.contacts()
                        .size()
        );

        /*
         * target = 6
         *
         * 4 XOR 6 = 2
         * 1 XOR 6 = 7
         * 8 XOR 6 = 14
         */

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

        server.stop();

        serverThread.join(2000);
    }


    private static Contact contact(
            int value
    ) {

        return new Contact(
                nodeId(value),
                "127.0.0.1",
                9300 + value,
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
