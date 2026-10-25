package ru.secureoverlay.secure_overlay.dht.bootstrap;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.lookup.IterativeNodeLookup;
import ru.secureoverlay.secure_overlay.dht.lookup.NodeLookupResult;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpc;

import java.io.IOException;

public class BootstrapService {
    private final NodeId localNodeId;
    private final RoutingTable routingTable;
    private final DhtRpc rpc;
    private final IterativeNodeLookup lookup;


    public BootstrapService(
            NodeId localNodeId,
            RoutingTable routingTable,
            DhtRpc rpc,
            IterativeNodeLookup lookup
    ) {

        this.localNodeId = localNodeId;

        this.routingTable = routingTable;

        this.rpc = rpc;

        this.lookup = lookup;
    }


    public NodeLookupResult bootstrap(Contact bootstrapContact) throws IOException {

        boolean alive =
                rpc.ping(
                        bootstrapContact.host(),
                        bootstrapContact.port()
                );

        if (!alive) {

            throw new IOException("Bootstrap node did not respond to PING");
        }

        routingTable.addOrRefresh(bootstrapContact);


        return lookup.lookup(localNodeId);
    }
}