package ru.secureoverlay.secure_overlay.dht.storage;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ru.secureoverlay.secure_overlay.dht.XorDistance.between;

public class FindValueService {

    private final RoutingTable routingTable;
    private final DhtRpc rpc;
    private final int alpha;


    public FindValueService(
            RoutingTable routingTable,
            DhtRpc rpc,
            int alpha
    ) {

        this.routingTable = routingTable;
        this.rpc = rpc;
        this.alpha = alpha;
    }


    public DhtRecord find(
            String key
    ) {

        NodeId target =
                DhtKey.fromString(
                        key
                );

        Map<NodeId, Contact> candidates =
                new HashMap<>();

        Set<NodeId> queried =
                new HashSet<>();


        routingTable
                .findClosest(
                        target,
                        alpha
                )
                .forEach(
                        contact ->
                                candidates.put(
                                        contact.nodeId(),
                                        contact
                                )
                );


        while (true) {

            var batch =
                    candidates.values()
                            .stream()

                            .filter(
                                    c ->
                                            !queried.contains(
                                                    c.nodeId()
                                            )
                            )

                            .sorted(
                                    Comparator.comparing(
                                            c ->
                                                    between(
                                                            c.nodeId(),
                                                            target
                                                    )
                                    )
                            )

                            .limit(alpha)

                            .toList();


            if (batch.isEmpty()) {
                return null;
            }


            for (Contact contact : batch) {

                queried.add(
                        contact.nodeId()
                );

                try {

                    FindValueResponse response =
                            rpc.findValue(
                                    contact.host(),
                                    contact.port(),
                                    key
                            );


                    if (response.found()) {

                        DhtRecord record =
                                response.record();

                        if (record != null
                                && DhtRecordSigner
                                .verify(record)
                                && !record.expired()) {

                            return record;
                        }
                    }


                    for (var dto :
                            response.contacts()) {

                        Contact discovered =
                                ContactMapper.fromDto(
                                        dto
                                );

                        candidates.putIfAbsent(
                                discovered.nodeId(),
                                discovered
                        );

                        routingTable.addOrRefresh(
                                discovered
                        );
                    }

                } catch (Exception ignored) {
                }
            }
        }
    }
}