package ru.secureoverlay.secure_overlay.experiment;

import ru.secureoverlay.secure_overlay.dht.lookup.NodeLookupResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExperimentRunner {

    private static final int NODE_COUNT =
            12;

    private static final int LOOKUPS =
            30;


    public List<LookupMetric> run(
            ExperimentTopology topology,
            int basePort
    ) throws Exception {

        List<LookupMetric> metrics =
                new ArrayList<>();


        try (ExperimentCluster cluster =
                     new ExperimentCluster(
                             NODE_COUNT,
                             basePort,
                             topology
                     )) {


            cluster.start();


            /*
             * Обычная работа сети.
             */
            metrics.addAll(
                    runLookups(
                            cluster,
                            topology,
                            "normal",
                            LOOKUPS,
                            12345
                    )
            );


            /*
             * Убиваем один узел.
             *
             * Не hub у STAR.
             */
            int failedNode =
                    topology
                            == ExperimentTopology.STAR
                            ? 6
                            : 6;


            cluster.stopNode(
                    failedNode
            );


            Thread.sleep(
                    300
            );


            /*
             * Повторяем после отказа.
             */
            metrics.addAll(
                    runLookups(
                            cluster,
                            topology,
                            "one-node-failed",
                            LOOKUPS,
                            54321
                    )
            );
        }


        return metrics;
    }


    private List<LookupMetric> runLookups(
            ExperimentCluster cluster,
            ExperimentTopology topology,
            String scenario,
            int count,
            long seed
    ) {

        List<LookupMetric> result =
                new ArrayList<>();


        Random random =
                new Random(seed);


        List<ExperimentNode> alive =
                cluster.aliveNodes();


        for (int run = 1;
             run <= count;
             run++) {


            ExperimentNode source =
                    alive.get(
                            random.nextInt(
                                    alive.size()
                            )
                    );


            ExperimentNode target;


            do {

                target =
                        alive.get(
                                random.nextInt(
                                        alive.size()
                                )
                        );

            } while (
                    source == target
            );


            long start =
                    System.nanoTime();


            NodeLookupResult lookupResult;


            try {

                lookupResult =
                        source.lookup(
                                target.nodeId()
                        );


            } catch (Exception e) {

                long latency =
                        (
                                System.nanoTime()
                                        - start
                        )
                                / 1_000_000;


                result.add(
                        new LookupMetric(
                                topology.name(),
                                scenario,
                                run,
                                shortId(
                                        source
                                ),
                                shortId(
                                        target
                                ),
                                false,
                                latency,
                                0,
                                0
                        )
                );


                continue;
            }


            long latency =
                    (
                            System.nanoTime()
                                    - start
                    )
                            / 1_000_000;


            boolean success =
                    lookupResult.isFound()
                            && lookupResult
                            .found()
                            .nodeId()
                            .equals(
                                    target.nodeId()
                            );


            result.add(
                    new LookupMetric(
                            topology.name(),
                            scenario,
                            run,
                            shortId(
                                    source
                            ),
                            shortId(
                                    target
                            ),
                            success,
                            latency,
                            lookupResult
                                    .rpcCount(),
                            lookupResult
                                    .iterations()
                    )
            );
        }


        return result;
    }


    private String shortId(
            ExperimentNode node
    ) {

        return node.nodeId()
                .toHex()
                .substring(
                        0,
                        12
                );
    }


    public static void main(String[] args) throws Exception {

        ExperimentRunner runner = new ExperimentRunner();


        List<LookupMetric> all = new ArrayList<>();


        System.out.println(
                "=== RING experiment ==="
        );


        all.addAll(
                runner.run(
                        ExperimentTopology.RING,
                        10_000
                )
        );


        System.out.println(
                "=== STAR experiment ==="
        );


        all.addAll(runner.run(
                        ExperimentTopology.STAR,
                        11_000
                )
        );


        Path output = Path.of(
                        "experiments",
                        "lookup-results.csv"
                );


        CsvMetricsWriter.write(
                output,
                all
        );


        System.out.println(
                "Experiment finished"
        );

        System.out.println(
                "Results: "
                        + output
                        .toAbsolutePath()
        );

        List<LookupMetric> ring =
                runner.run(
                        ExperimentTopology.RING,
                        10_000
                );

        ExperimentSummary.print(
                ring
        );

        all.addAll(
                ring
        );


        List<LookupMetric> star =
                runner.run(
                        ExperimentTopology.STAR,
                        11_000
                );

        ExperimentSummary.print(
                star
        );

        all.addAll(
                star
        );
    }
}