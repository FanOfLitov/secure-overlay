package ru.secureoverlay.secure_overlay.dht;


import ru.secureoverlay.secure_overlay.dht.rpc.ContactDto;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.util.List;

public class FindNodeService {

    private final RoutingTable routingTable;
    private final int resultLimit;

    public FindNodeService(
            RoutingTable routingTable,
            int resultLimit
    ){
        this.routingTable=routingTable;
        this.resultLimit=resultLimit;
    }

    public FindNodeResponse find(
            NodeId target
    ){
        List<ContactDto>contacts=
                routingTable
                        .findClosest(
                                target,
                                resultLimit
                        )
                        .stream()
                        .map(ContactMapper::toDto)
                        .toList();

        return new FindNodeResponse(
                contacts
        );
    }
}
