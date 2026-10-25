package ru.secureoverlay.secure_overlay.dht.storage;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.util.List;

public class DhtStorageService {

    private final DhtStore store;
    private final RoutingTable routingTable;
    private final int contactLimit;


    public DhtStorageService(
            DhtStore store,
            RoutingTable routingTable,
            int contactLimit
    ) {

        this.store = store;
        this.routingTable = routingTable;
        this.contactLimit = contactLimit;
    }


    public StoreResult store(
            DhtRecord record
    ) {

        return store.put(record);
    }


    public FindValueResponse findValue(
            String key
    ) {

        var value =
                store.get(key);

        if (value.isPresent()) {

            return new FindValueResponse(
                    true,
                    value.get(),
                    List.of()
            );
        }


        NodeId target =
                DhtKey.fromString(
                        key
                );

        List<Contact> closest =
                routingTable.findClosest(
                        target,
                        contactLimit
                );


        return new FindValueResponse(
                false,
                null,
                closest.stream()
                        .map(
                                ContactMapper::toDto
                        )
                        .toList()
        );
    }
}