package ru.secureoverlay.secure_overlay.dht.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreResponse;
import ru.secureoverlay.secure_overlay.identity.IdentityService;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.identity.NodeIdentity;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DhtStorageIntegrationTest {

    @TempDir
    Path tempDir;


    @Test
    void shouldStoreAndFindValueOverTcp()
            throws Exception {

        int port = 9801;

        NodeId nodeId =
                nodeId(1);

        RoutingTable routingTable =
                new RoutingTable(
                        nodeId,
                        3
                );

        TcpServer server =
                new TcpServer(
                        port,
                        routingTable
                );

        Thread thread =
                new Thread(
                        server::start
                );

        thread.start();

        Thread.sleep(300);


        NodeIdentity identity =
                new IdentityService()
                        .loadOrCreate(
                                tempDir
                        );


        DhtRecord record =
                DhtRecordSigner.sign(
                        "user:ivan",
                        "hello-overlay",
                        1,
                        System.currentTimeMillis()
                                + 60_000,
                        identity
                );


        DhtRpcClient rpc =
                new DhtRpcClient();


        StoreResponse storeResponse =
                rpc.store(
                        "127.0.0.1",
                        port,
                        record
                );


        assertTrue(
                storeResponse.stored()
        );


        FindValueResponse valueResponse =
                rpc.findValue(
                        "127.0.0.1",
                        port,
                        "user:ivan"
                );


        assertTrue(
                valueResponse.found()
        );

        assertNotNull(
                valueResponse.record()
        );

        assertEquals(
                "hello-overlay",
                valueResponse
                        .record()
                        .value()
        );


        server.stop();

        thread.join(2000);
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