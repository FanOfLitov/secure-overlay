package ru.secureoverlay.secure_overlay.dht.lookup;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.dht.rpc.ContactMapper;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.FindValueResponse;
import ru.secureoverlay.secure_overlay.dht.rpc.StoreResponse;
import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.DhtRpc;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IterativeNodeLookupTest {

    @Test
    void shouldFindTargetThroughSeveralHops() {

        Contact b =
                contact(
                        2,
                        9502
                );

        Contact c =
                contact(
                        4,
                        9503
                );

        Contact d =
                contact(
                        8,
                        9504
                );

        Contact e =
                contact(
                        16,
                        9505
                );


        RoutingTable table =
                new RoutingTable(
                        nodeId(1),
                        3
                );


        /*
         * Узел A изначально знает только B.
         */
        table.addOrRefresh(b);


        DhtRpc rpc =
                new ChainRpc(
                        Map.of(
                                9502,
                                response(c),

                                9503,
                                response(d),

                                9504,
                                response(e),

                                9505,
                                emptyResponse()
                        )
                );


        IterativeNodeLookup lookup =
                new IterativeNodeLookup(
                        table,
                        rpc,
                        3,
                        3
                );


        NodeLookupResult result =
                lookup.lookup(
                        nodeId(16)
                );


        lookup.shutdown();


        assertTrue(
                result.isFound()
        );


        assertEquals(
                nodeId(16),
                result.found()
                        .nodeId()
        );


        /*
         * Должны пройти через несколько
         * промежуточных узлов.
         */
        assertTrue(
                result.rpcCount() >= 3
        );


        assertTrue(
                result.iterations() >= 3
        );
    }


    @Test
    void shouldQueryUpToThreeContactsInParallel()
            throws Exception {

        RoutingTable table =
                new RoutingTable(
                        nodeId(0),
                        3
                );


        table.addOrRefresh(
                contact(
                        4,
                        9601
                )
        );

        table.addOrRefresh(
                contact(
                        8,
                        9602
                )
        );

        table.addOrRefresh(
                contact(
                        16,
                        9603
                )
        );


        ParallelRpc rpc =
                new ParallelRpc();


        IterativeNodeLookup lookup =
                new IterativeNodeLookup(
                        table,
                        rpc,
                        3,
                        3
                );


        lookup.lookup(
                nodeId(100)
        );


        lookup.shutdown();


        assertEquals(
                3,
                rpc.maxParallelCalls()
        );
    }


    /*
     * Fake RPC для проверки
     * многошагового lookup.
     */
    private static class ChainRpc
            implements DhtRpc {

        private final Map<Integer, FindNodeResponse>
                responses;


        private ChainRpc(
                Map<Integer, FindNodeResponse> responses
        ) {
            this.responses =
                    responses;
        }


        @Override
        public boolean ping(
                String host,
                int port
        ) {
            return true;
        }


        @Override
        public FindNodeResponse findNode(
                String host,
                int port,
                NodeId target
        ) {

            return responses.getOrDefault(
                    port,
                    emptyResponse()
            );
        }


        @Override
        public StoreResponse store(
                String host,
                int port,
                DhtRecord record
        ) {

            throw new UnsupportedOperationException(
                    "STORE is not used in this test"
            );
        }


        @Override
        public FindValueResponse findValue(
                String host,
                int port,
                String key
        ) {

            throw new UnsupportedOperationException(
                    "FIND_VALUE is not used in this test"
            );
        }
    }


    /*
     * Fake RPC для проверки того,
     * что ALPHA=3 реально выполняется
     * параллельно.
     */
    private static class ParallelRpc
            implements DhtRpc {

        private final AtomicInteger active =
                new AtomicInteger();

        private final AtomicInteger maxActive =
                new AtomicInteger();

        private final CountDownLatch started =
                new CountDownLatch(3);


        @Override
        public boolean ping(
                String host,
                int port
        ) {
            return true;
        }


        @Override
        public FindNodeResponse findNode(
                String host,
                int port,
                NodeId target
        ) throws IOException {

            int current =
                    active.incrementAndGet();


            maxActive.accumulateAndGet(
                    current,
                    Math::max
            );


            started.countDown();


            try {

                /*
                 * Ждём, пока все три вызова
                 * одновременно стартуют.
                 */
                started.await(
                        2,
                        TimeUnit.SECONDS
                );


                /*
                 * Даём потокам немного
                 * перекрыться по времени.
                 */
                Thread.sleep(100);


            } catch (InterruptedException e) {

                Thread.currentThread()
                        .interrupt();

                throw new IOException(e);


            } finally {

                active.decrementAndGet();
            }


            return new FindNodeResponse(
                    List.of()
            );
        }


        @Override
        public StoreResponse store(
                String host,
                int port,
                DhtRecord record
        ) {

            throw new UnsupportedOperationException(
                    "STORE is not used in this test"
            );
        }


        @Override
        public FindValueResponse findValue(
                String host,
                int port,
                String key
        ) {

            throw new UnsupportedOperationException(
                    "FIND_VALUE is not used in this test"
            );
        }


        int maxParallelCalls() {
            return maxActive.get();
        }
    }


    private static FindNodeResponse response(
            Contact contact
    ) {

        return new FindNodeResponse(
                List.of(
                        ContactMapper.toDto(
                                contact
                        )
                )
        );
    }


    private static FindNodeResponse emptyResponse() {

        return new FindNodeResponse(
                List.of()
        );
    }


    private static Contact contact(
            int value,
            int port
    ) {

        return new Contact(
                nodeId(value),
                "127.0.0.1",
                port,
                Instant.now()
        );
    }


    private static NodeId nodeId(
            int value
    ) {

        byte[] bytes =
                new byte[
                        NodeId.LENGTH_BYTES
                        ];


        bytes[31] =
                (byte) value;


        return new NodeId(
                bytes
        );
    }
}