package ru.secureoverlay.secure_overlay.experiment;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.RoutingTable;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.transport.TcpServer;
import ru.secureoverlay.secure_overlay.transport.TunnelRpcClient;
import ru.secureoverlay.secure_overlay.tunnel.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TunnelExperimentRunner {

    private static final int NORMAL_RUNS = 30;
    private static final int FAILURE_RUNS = 5;


    public static void main(String[] args)
            throws Exception {

        TunnelExperimentRunner runner =
                new TunnelExperimentRunner();

        List<TunnelMetric> metrics =
                runner.run();

        Path output =
                Path.of(
                        "experiments",
                        "tunnel-results.csv"
                );

        TunnelMetricsWriter.write(
                output,
                metrics
        );

        printSummary(
                metrics
        );

        System.out.println(
                "[EXPERIMENT] Results written to: "
                        + output.toAbsolutePath()
        );
    }


    public List<TunnelMetric> run()
            throws Exception {

        List<TunnelMetric> metrics =
                new ArrayList<>();


        /*
         * Первая группа:
         * 30 обычных передач.
         */
        for (int i = 1;
             i <= NORMAL_RUNS;
             i++) {

            metrics.add(
                    runNormal(
                            i,
                            13_000
                                    + i * 10
                    )
            );
        }


        /*
         * Вторая группа:
         * минимум 5 отказов relay.
         */
        for (int i = 1;
             i <= FAILURE_RUNS;
             i++) {

            metrics.add(
                    runFailure(
                            i,
                            15_000
                                    + i * 10
                    )
            );
        }


        return metrics;
    }


    private TunnelMetric runNormal(
            int run,
            int basePort
    ) throws Exception {

        ExperimentEnvironment env =
                createEnvironment(
                        basePort
                );


        try {

            env.start();


            TunnelClient client =
                    new TunnelClient(
                            new TunnelRpcClient()
                    );


            TunnelBuildMetric buildMetric =
                    client.buildMeasured(
                            env.primaryRoute()
                    );


            long sendStart =
                    System.nanoTime();


            var ack =
                    client.send(
                            buildMetric.tunnel(),
                            (
                                    "NORMAL-MESSAGE-"
                                            + run
                            )
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );


            long latency =
                    (
                            System.nanoTime()
                                    - sendStart
                    )
                            / 1_000_000;


            return new TunnelMetric(
                    run,
                    "normal",
                    ack.delivered(),
                    buildMetric.buildTimeMs(),
                    latency,
                    0
            );


        } finally {

            env.close();
        }
    }


    private TunnelMetric runFailure(
            int run,
            int basePort
    ) throws Exception {

        ExperimentEnvironment env =
                createEnvironment(
                        basePort
                );


        try {

            env.start();


            TunnelClient baseClient =
                    new TunnelClient(
                            new TunnelRpcClient()
                    );


            ResilientTunnelClient client =
                    new ResilientTunnelClient(
                            baseClient,
                            env.primaryRoute(),
                            env.backupRoute()
                    );


            TunnelBuildMetric build =
                    client.connect();


            /*
             * Проверяем рабочий tunnel
             * до отказа.
             */
            ResilientSendResult first =
                    client.send(
                            (
                                    "BEFORE-FAILURE-"
                                            + run
                            )
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );


            if (!first.delivered()) {

                return new TunnelMetric(
                        run,
                        "relay-failure",
                        false,
                        build.buildTimeMs(),
                        0,
                        0
                );
            }


            /*
             * Ломаем второй relay
             * активного туннеля.
             */
            env.primaryRelay2()
                    .stop();


            Thread.sleep(
                    100
            );


            long sendStart =
                    System.nanoTime();


            ResilientSendResult recovered =
                    client.send(
                            (
                                    "AFTER-FAILURE-"
                                            + run
                            )
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );


            long totalLatency =
                    (
                            System.nanoTime()
                                    - sendStart
                    )
                            / 1_000_000;


            return new TunnelMetric(
                    run,
                    "relay-failure",
                    recovered.delivered(),
                    build.buildTimeMs(),
                    totalLatency,
                    recovered.recoveryTimeMs()
            );


        } finally {

            env.close();
        }
    }


    private ExperimentEnvironment createEnvironment(
            int basePort
    ) {

        Contact relay1 =
                contact(
                        nodeId(
                                basePort + 1
                        ),
                        basePort + 1
                );

        Contact relay2 =
                contact(
                        nodeId(
                                basePort + 2
                        ),
                        basePort + 2
                );

        Contact relay3 =
                contact(
                        nodeId(
                                basePort + 3
                        ),
                        basePort + 3
                );

        Contact relay4 =
                contact(
                        nodeId(
                                basePort + 4
                        ),
                        basePort + 4
                );

        Contact destination =
                contact(
                        nodeId(
                                basePort + 5
                        ),
                        basePort + 5
                );


        return new ExperimentEnvironment(
                relay1,
                relay2,
                relay3,
                relay4,
                destination
        );
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


    private static NodeId nodeId(
            int value
    ) {

        byte[] bytes =
                new byte[
                        NodeId.LENGTH_BYTES
                        ];


        bytes[30] =
                (byte)
                        (
                                value >>> 8
                        );

        bytes[31] =
                (byte) value;


        return new NodeId(
                bytes
        );
    }


    private static void printSummary(
            List<TunnelMetric> metrics
    ) {

        List<Long> normalLatency =
                metrics.stream()
                        .filter(
                                m ->
                                        m.scenario()
                                                .equals(
                                                        "normal"
                                                )
                        )
                        .map(
                                TunnelMetric
                                        ::endToEndLatencyMs
                        )
                        .toList();


        List<Long> buildTimes =
                metrics.stream()
                        .map(
                                TunnelMetric
                                        ::buildTimeMs
                        )
                        .toList();


        List<Long> recoveryTimes =
                metrics.stream()
                        .filter(
                                m ->
                                        m.scenario()
                                                .equals(
                                                        "relay-failure"
                                                )
                        )
                        .map(
                                TunnelMetric
                                        ::recoveryTimeMs
                        )
                        .toList();


        long delivered =
                metrics.stream()
                        .filter(
                                TunnelMetric::delivered
                        )
                        .count();


        System.out.println();
        System.out.println(
                "========== TUNNEL EXPERIMENT =========="
        );

        System.out.println(
                "Total observations: "
                        + metrics.size()
        );

        System.out.println(
                "Delivered: "
                        + delivered
                        + "/"
                        + metrics.size()
        );


        printStatistics(
                "Tunnel build",
                buildTimes
        );


        printStatistics(
                "E2E latency",
                normalLatency
        );


        printStatistics(
                "Recovery",
                recoveryTimes
        );


        System.out.println(
                "======================================="
        );
    }


    private static void printStatistics(
            String name,
            List<Long> values
    ) {

        if (values.isEmpty()) {
            return;
        }


        System.out.println();
        System.out.println(
                name + ":"
        );


        System.out.printf(
                "  mean   = %.2f ms%n",
                Statistics.mean(
                        values
                )
        );


        System.out.printf(
                "  median = %.2f ms%n",
                Statistics.median(
                        values
                )
        );


        System.out.println(
                "  min    = "
                        + Statistics.min(
                        values
                )
                        + " ms"
        );


        System.out.println(
                "  max    = "
                        + Statistics.max(
                        values
                )
                        + " ms"
        );


        /*
         * p95 выводим только при N >= 20.
         */
        if (values.size() >= 20) {

            System.out.println(
                    "  p95    = "
                            + Statistics
                            .percentile95(
                                    values
                            )
                            + " ms"
            );
        }
    }


    /*
     * Вспомогательный класс одного
     * экспериментального стенда.
     */
    private static class ExperimentEnvironment
            implements AutoCloseable {

        private final Contact relay1;
        private final Contact relay2;
        private final Contact relay3;
        private final Contact relay4;
        private final Contact destination;

        private final TcpServer relay1Server;
        private final TcpServer relay2Server;
        private final TcpServer relay3Server;
        private final TcpServer relay4Server;
        private final TcpServer destinationServer;

        private final List<Thread>
                threads =
                new ArrayList<>();


        ExperimentEnvironment(
                Contact relay1,
                Contact relay2,
                Contact relay3,
                Contact relay4,
                Contact destination
        ) {

            this.relay1 =
                    relay1;

            this.relay2 =
                    relay2;

            this.relay3 =
                    relay3;

            this.relay4 =
                    relay4;

            this.destination =
                    destination;


            relay1Server =
                    server(
                            relay1
                    );

            relay2Server =
                    server(
                            relay2
                    );

            relay3Server =
                    server(
                            relay3
                    );

            relay4Server =
                    server(
                            relay4
                    );

            destinationServer =
                    server(
                            destination
                    );
        }


        void start()
                throws InterruptedException {

            threads.add(
                    startServer(
                            relay1Server
                    )
            );

            threads.add(
                    startServer(
                            relay2Server
                    )
            );

            threads.add(
                    startServer(
                            relay3Server
                    )
            );

            threads.add(
                    startServer(
                            relay4Server
                    )
            );

            threads.add(
                    startServer(
                            destinationServer
                    )
            );


            Thread.sleep(
                    250
            );
        }


        List<Contact> primaryRoute() {

            return List.of(
                    relay1,
                    relay2,
                    destination
            );
        }


        List<Contact> backupRoute() {

            return List.of(
                    relay3,
                    relay4,
                    destination
            );
        }


        TcpServer primaryRelay2() {

            return relay2Server;
        }


        @Override
        public void close() {

            relay1Server.stop();
            relay2Server.stop();
            relay3Server.stop();
            relay4Server.stop();
            destinationServer.stop();


            for (Thread thread : threads) {

                try {

                    thread.join(
                            1000
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();
                }
            }
        }


        private static TcpServer server(
                Contact contact
        ) {

            return new TcpServer(
                    contact.port(),
                    new RoutingTable(
                            contact.nodeId(),
                            3
                    )
            );
        }


        private static Thread startServer(
                TcpServer server
        ) {

            Thread thread =
                    new Thread(
                            server::start
                    );

            thread.start();

            return thread;
        }
    }
}