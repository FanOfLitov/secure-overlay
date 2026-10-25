package ru.secureoverlay.secure_overlay.experiment;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.lookup.IterativeNodeLookup;
import ru.secureoverlay.secure_overlay.dht.lookup.NodeLookupResult;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;
import ru.secureoverlay.secure_overlay.transport.TcpServer;

import java.time.Instant;

public class ExperimentNode {

    private final int index;

    private final NodeId nodeId;

    private final int port;

    private final RoutingTable routingTable;

    private final TcpServer server;

    private final Thread serverThread;

    private final DhtRpcClient rpcClient;

    private final IterativeNodeLookup lookup;

    private volatile boolean alive = true;


    public ExperimentNode(
            int index,
            NodeId nodeId,
            int port,
            int bucketCapacity
    ) {

        this.index = index;

        this.nodeId = nodeId;

        this.port = port;


        this.routingTable = new RoutingTable(
                        nodeId,
                        bucketCapacity
                );


        this.server = new TcpServer(
                        port,
                        routingTable
                );


        this.serverThread = new Thread(
                        server::start,
                        "experiment-node-" + index
                );


        this.rpcClient = new DhtRpcClient();


        this.lookup = new IterativeNodeLookup(
                        routingTable,
                        rpcClient,
                        3,
                        3
                );
    }


    public void start() {

        serverThread.start();
    }


    public boolean addContact(
            ExperimentNode other
    ) {

        return routingTable.addOrRefresh(other.asContact()
        );
    }


    public Contact asContact() {

        return new Contact(
                nodeId,
                "127.0.0.1",
                port,
                Instant.now()
        );
    }


    public NodeLookupResult lookup(NodeId target
    ) {

        return lookup.lookup(target
        );
    }


    public void stop() {

        if (!alive) {
            return;
        }

        alive = false;

        server.stop();

        lookup.shutdown();


        try {

            serverThread.join(
                    2000
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }


    public int index() {
        return index;
    }


    public NodeId nodeId() {
        return nodeId;
    }


    public int port() {
        return port;
    }


    public RoutingTable routingTable() {
        return routingTable;
    }


    public boolean alive() {
        return alive;
    }
}