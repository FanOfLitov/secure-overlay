package ru.secureoverlay.secure_overlay.experiment;

import java.util.ArrayList;
import java.util.List;

public class ExperimentCluster
        implements AutoCloseable {

    private final List<ExperimentNode> nodes = new ArrayList<>();


    private final ExperimentTopology topology;


    public ExperimentCluster(
            int nodeCount,
            int basePort,
            ExperimentTopology topology
    ) {

        if (nodeCount < 5) {

            throw new IllegalArgumentException(
                    "Experiment requires at least 5 nodes"
            );
        }


        this.topology = topology;


        for (int i = 0;
             i < nodeCount;
             i++) {


            int bucketCapacity =
                    topology
                            == ExperimentTopology.RING
                            ? 3
                            : nodeCount;


            nodes.add(
                    new ExperimentNode(
                            i,
                            ExperimentNodeIds
                                    .forIndex(i),
                            basePort + i,
                            bucketCapacity
                    )
            );
        }
    }


    public void start()
            throws InterruptedException {

        for (ExperimentNode node : nodes) {

            node.start();
        }


        Thread.sleep(
                700
        );


        switch (topology) {

            case RING ->
                    buildRing();

            case STAR ->
                    buildStar();
        }
    }


    private void buildRing() {

        int count =
                nodes.size();


        for (int i = 0;
             i < count;
             i++) {

            ExperimentNode current =
                    nodes.get(i);


            /*
             * predecessor
             */
            ExperimentNode previous =
                    nodes.get(
                            (i - 1 + count)
                                    % count
                    );


            /*
             * successor
             */
            ExperimentNode next =
                    nodes.get(
                            (i + 1)
                                    % count
                    );



            ExperimentNode opposite =
                    nodes.get(
                            (i + count / 2)
                                    % count
                    );


            current.addContact(
                    previous
            );

            current.addContact(
                    next
            );

            current.addContact(
                    opposite
            );
        }
    }


    private void buildStar() {

        ExperimentNode hub =
                nodes.get(0);


        for (int i = 1;
             i < nodes.size();
             i++) {

            ExperimentNode leaf =
                    nodes.get(i);


            leaf.addContact(
                    hub
            );


            hub.addContact(
                    leaf
            );
        }
    }


    public ExperimentNode get(
            int index
    ) {

        return nodes.get(
                index
        );
    }


    public List<ExperimentNode>
    aliveNodes() {

        return nodes.stream()
                .filter(
                        ExperimentNode::alive
                )
                .toList();
    }


    public int size() {

        return nodes.size();
    }


    public void stopNode(
            int index
    ) {

        nodes.get(index)
                .stop();
    }


    @Override
    public void close() {

        for (ExperimentNode node : nodes) {

            node.stop();
        }
    }
}