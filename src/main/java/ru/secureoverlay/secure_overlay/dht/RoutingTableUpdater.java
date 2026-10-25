package ru.secureoverlay.secure_overlay.dht;

import ru.secureoverlay.secure_overlay.transport.DhtRpc;
public class RoutingTableUpdater {

    private final RoutingTable routingTable;
    private final DhtRpc rpc;

    public RoutingTableUpdater(RoutingTable routingTable, DhtRpc rpc){
        this.routingTable = routingTable;
        this.rpc = rpc;
    }

    public boolean addContact(Contact newContact) {

        int bucketIndex = routingTable.bucketIndexFor(newContact.nodeId());

        KBucket bucket = routingTable.getBucket(bucketIndex);

        if (bucket.contains(newContact.nodeId())) {

            bucket.addOrRefresh(newContact);

            return true;
        }

        if (bucket.size() < bucket.capacity()) {

            return bucket.addOrRefresh(newContact);
        }

        Contact lru = bucket.leastRecentlySeen();

        boolean alive = false;

        try {

            alive = rpc.ping(
                            lru.host(),
                            lru.port()
                    );

        } catch (Exception ignored) {
        }


        if (alive) {

            bucket.addOrRefresh(lru);

            return false;
        }


        bucket.remove(lru.nodeId());

        return bucket.addOrRefresh(newContact);
    }
}

