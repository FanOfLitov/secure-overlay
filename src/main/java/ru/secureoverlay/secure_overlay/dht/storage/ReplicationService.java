package ru.secureoverlay.secure_overlay.dht.storage;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.lookup.IterativeNodeLookup;
import ru.secureoverlay.secure_overlay.dht.lookup.NodeLookupResult;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpc;

import java.util.List;

public class ReplicationService {

    private final IterativeNodeLookup lookup;

    private final DhtRpc rpc;

    private final int replicationFactor;


    public ReplicationService(
            IterativeNodeLookup lookup,
            DhtRpc rpc,
            int replicationFactor
    ) {

        if (replicationFactor <= 0) {

            throw new IllegalArgumentException(
                    "Replication factor must be positive"
            );
        }

        this.lookup =
                lookup;

        this.rpc =
                rpc;

        this.replicationFactor =
                replicationFactor;
    }


    public ReplicationResult replicate(
            DhtRecord record
    ) {

        NodeId target =
                DhtKey.fromString(
                        record.key()
                );


        NodeLookupResult lookupResult =
                lookup.lookup(
                        target
                );


        List<Contact> holders =
                lookupResult
                        .closest()
                        .stream()
                        .limit(
                                replicationFactor
                        )
                        .toList();


        int successful = 0;


        for (Contact contact : holders) {

            try {

                StoreResponse response =
                        rpc.store(
                                contact.host(),
                                contact.port(),
                                record
                        );


                if (response.stored()) {

                    successful++;
                }


            } catch (Exception e) {

                System.err.println(
                        "[REPLICATION] Cannot store at "
                                + contact.host()
                                + ":"
                                + contact.port()
                                + " - "
                                + e.getMessage()
                );
            }
        }


        return new ReplicationResult(
                holders.size(),
                successful,
                replicationFactor
        );
    }
}