package ru.secureoverlay.secure_overlay.security.tls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreResponse;
import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;
import ru.secureoverlay.secure_overlay.dht.storage.DhtRecordSigner;
import ru.secureoverlay.secure_overlay.identity.IdentityService;
import ru.secureoverlay.secure_overlay.identity.NodeIdentity;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TlsDhtIntegrationTest {

    @TempDir
    Path tempDir;


    @Test
    void shouldStoreAndFindValueOverMutualTls() throws Exception {

        int port = 9901;


        NodeIdentity serverIdentity = new IdentityService().loadOrCreate(tempDir.resolve("server")
                        );


        NodeIdentity clientIdentity = new IdentityService().loadOrCreate(tempDir.resolve(
                                        "client"
                                )
                        );


        SSLContext serverTls =
                TlsContextFactory.create(
                        serverIdentity
                );


        SSLContext clientTls =
                TlsContextFactory.create(
                        clientIdentity
                );


        RoutingTable serverTable =
                new RoutingTable(
                        serverIdentity.nodeId(),
                        3
                );


        TcpServer server =
                new TcpServer(
                        port,
                        serverTable,
                        serverTls
                                .getServerSocketFactory()
                );


        Thread thread =
                new Thread(
                        server::start
                );


        thread.start();


        Thread.sleep(400);


        try {

            DhtRpcClient client =
                    new DhtRpcClient(
                            clientTls
                                    .getSocketFactory()
                    );


            DhtRecord record =
                    DhtRecordSigner.sign(
                            "tls-key",
                            "SECRET-VALUE",
                            1,
                            System.currentTimeMillis()
                                    + 60_000,
                            clientIdentity
                    );


            StoreResponse store = client.store(
                            "127.0.0.1",
                            port,
                            record
                    );


            assertTrue(store.stored()
            );


            FindValueResponse response =
                    client.findValue(
                            "127.0.0.1",
                            port,
                            "tls-key"
                    );


            assertTrue(response.found()
            );


            assertNotNull(response.record()
            );


            assertEquals("SECRET-VALUE", response
                            .record()
                            .value()
            );


        } finally {

            server.stop();

            thread.join(2000);
        }
    }
}