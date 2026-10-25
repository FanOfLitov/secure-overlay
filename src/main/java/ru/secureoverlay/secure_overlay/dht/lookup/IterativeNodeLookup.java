package ru.secureoverlay.secure_overlay.dht.lookup;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.XorDistance;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpc;
import ru.secureoverlay.secure_overlay.transport.DhtRpcClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IterativeNodeLookup {

    private final RoutingTable routingTable;
    private final DhtRpc rpcClient;

    private final int alpha;
    private final int resultLimit;

    private final ExecutorService executor;


    public IterativeNodeLookup(
            RoutingTable routingTable,
            DhtRpc rpcClient,
            int alpha,
            int resultLimit
    ) {

        if (alpha <= 0) {
            throw new IllegalArgumentException(
                    "Alpha must be positive"
            );
        }

        if (resultLimit <= 0) {
            throw new IllegalArgumentException(
                    "Result limit must be positive"
            );
        }

        this.routingTable = routingTable;
        this.rpcClient = rpcClient;
        this.alpha = alpha;
        this.resultLimit = resultLimit;

        this.executor = Executors.newFixedThreadPool(alpha);
    }


    public NodeLookupResult lookup(NodeId target) {

        Map<NodeId, Contact> candidates = new HashMap<>();

        Set<NodeId> queried = new HashSet<>();

        int rpcCount = 0;
        int iterations = 0;

        for (Contact contact :
                routingTable.findClosest(
                        target,
                        resultLimit
                )) {

            candidates.put(
                    contact.nodeId(),
                    contact
            );
        }


        while (true) {

            Contact targetContact = candidates.get(target);

            if (targetContact != null) {

                return new NodeLookupResult(
                        targetContact,
                        closest(
                                candidates,
                                target
                        ),
                        rpcCount,
                        iterations
                );
            }


            List<Contact> batch = candidates.values()
                            .stream()

                            .filter(
                                    contact ->
                                            !queried.contains(
                                                    contact.nodeId()
                                            )
                            )

                            .sorted(
                                    Comparator.comparing(
                                            contact ->
                                                    XorDistance.between(
                                                            contact.nodeId(),
                                                            target
                                                    )
                                    )
                            )

                            .limit(alpha)

                            .toList();



            if (batch.isEmpty()) {

                return new NodeLookupResult(
                        null,
                        closest(
                                candidates,
                                target
                        ),
                        rpcCount,
                        iterations
                );
            }


            iterations++;

            for (Contact contact : batch) {
                queried.add(contact.nodeId());
            }


            List<CompletableFuture<LookupRpcResult>>
                    futures =  new ArrayList<>();


            for (Contact contact : batch) {

                CompletableFuture<LookupRpcResult>
                        future = CompletableFuture.supplyAsync(
                                () -> queryContact(
                                        contact,
                                        target
                                ),
                                executor
                        );

                futures.add(future);
            }



            for (CompletableFuture<LookupRpcResult>
                    future : futures) {

                LookupRpcResult rpcResult =  future.join();

                rpcCount++;

                if (!rpcResult.successful()) {

                    System.err.println(
                            "[LOOKUP] Failed to query "
                                    + rpcResult
                                    .queriedContact()
                                    .nodeId()
                    );

                    continue;
                }


                FindNodeResponse response =  rpcResult.response();


                for (var dto : response.contacts()) {

                    Contact discovered = ContactMapper.fromDto(dto);

                    candidates.putIfAbsent(discovered.nodeId(), discovered);


                    routingTable.addOrRefresh(discovered);
                }
            }
        }
    }


    private LookupRpcResult queryContact(Contact contact, NodeId target) {

        try {

            FindNodeResponse response = rpcClient.findNode(
                            contact.host(),
                            contact.port(),
                            target
                    );

            return new LookupRpcResult(contact, response, null);

        } catch (Exception e) {

            return new LookupRpcResult(contact, null, e);
        }
    }


    private List<Contact> closest(Map<NodeId, Contact> candidates, NodeId target) {

        return candidates
                .values()
                .stream()

                .sorted(
                        Comparator.comparing(
                                contact ->
                                        XorDistance.between(
                                                contact.nodeId(),
                                                target
                                        )
                        )
                )

                .limit(resultLimit)

                .toList();
    }


    public void shutdown() {
        executor.shutdown();
    }
}