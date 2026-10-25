package ru.secureoverlay.secure_overlay.dht.bootstrap;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.lookup.IterativeNodeLookup;
import ru.secureoverlay.secure_overlay.dht.lookup.NodeLookupResult;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FiveNodeBootstrapIntegrationTest {

    @Test
    void shouldDiscoverNetworkAndWorkAfterSeedStops()
            throws Exception {

        NodeId aId = nodeId(1);
        NodeId bId = nodeId(2);
        NodeId cId = nodeId(4);
        NodeId dId = nodeId(8);
        NodeId eId = nodeId(16);

        int bPort = 9702;
        int cPort = 9703;
        int dPort = 9704;
        int ePort = 9705;


        RoutingTable bTable = new RoutingTable(
                        bId,
                        3
                );

        RoutingTable cTable =
                new RoutingTable(
                        cId,
                        3
                );

        RoutingTable dTable =
                new RoutingTable(
                        dId,
                        3
                );

        RoutingTable eTable =
                new RoutingTable(
                        eId,
                        3
                );



        bTable.addOrRefresh(
                contact(
                        cId,
                        cPort
                )
        );

        cTable.addOrRefresh(
                contact(
                        dId,
                        dPort
                )
        );

        dTable.addOrRefresh(
                contact(
                        eId,
                        ePort
                )
        );


        TcpServer bServer =
                new TcpServer(
                        bPort,
                        bTable
                );

        TcpServer cServer =
                new TcpServer(
                        cPort,
                        cTable
                );

        TcpServer dServer =
                new TcpServer(
                        dPort,
                        dTable
                );

        TcpServer eServer =
                new TcpServer(
                        ePort,
                        eTable
                );


        Thread bThread = new Thread(bServer::start);

        Thread cThread = new Thread(cServer::start);

        Thread dThread = new Thread(dServer::start);

        Thread eThread = new Thread(eServer::start);


        bThread.start();
        cThread.start();
        dThread.start();
        eThread.start();

        Thread.sleep(500);



        RoutingTable aTable = new RoutingTable(aId, 3);

        DhtRpcClient rpc =
                new DhtRpcClient();

        IterativeNodeLookup lookup =
                new IterativeNodeLookup(
                        aTable,
                        rpc,
                        3,
                        3
                );

        BootstrapService bootstrap =
                new BootstrapService(
                        aId,
                        aTable,
                        rpc,
                        lookup
                );



        Contact seed = contact(bId, bPort);


        bootstrap.bootstrap(seed);



        assertTrue(
                aTable.totalContacts() >= 2
        );



        bServer.stop();

        bThread.join(2000);



        NodeLookupResult result = lookup.lookup(eId);


        assertTrue(result.isFound()
        );

        assertEquals(eId, result.found().nodeId()
        );


        lookup.shutdown();

        cServer.stop();
        dServer.stop();
        eServer.stop();

        cThread.join(2000);
        dThread.join(2000);
        eThread.join(2000);
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


    private static NodeId nodeId(int value) {

        byte[] bytes = new byte[NodeId.LENGTH_BYTES];

        bytes[31] = (byte) value;

        return new NodeId(bytes);
    }
}