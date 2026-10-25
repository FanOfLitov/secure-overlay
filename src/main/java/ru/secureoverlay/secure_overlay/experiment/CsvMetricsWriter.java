package ru.secureoverlay.secure_overlay.experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvMetricsWriter {

    private CsvMetricsWriter() {
    }


    public static void write(Path file, List<LookupMetric> metrics
    ) throws IOException {

        if (file.getParent() != null) {

            Files.createDirectories(file.getParent());
        }


        StringBuilder csv = new StringBuilder();


        csv.append(
                "topology,"
                        + "scenario,"
                        + "run,"
                        + "source_node_id,"
                        + "target_node_id,"
                        + "success,"
                        + "latency_ms,"
                        + "rpc_count,"
                        + "iterations\n"
        );


        for (LookupMetric metric : metrics) {

            csv.append(metric.topology()
            );

            csv.append(",");

            csv.append(metric.scenario()
            );

            csv.append(",");

            csv.append(metric.run()
            );

            csv.append(",");

            csv.append(metric.sourceNodeId()
            );

            csv.append(",");

            csv.append(metric.targetNodeId()
            );

            csv.append(",");

            csv.append(metric.success()
            );

            csv.append(",");

            csv.append(metric.latencyMs()
            );

            csv.append(",");

            csv.append(metric.rpcCount()
            );

            csv.append(",");

            csv.append(metric.iterations()
            );

            csv.append("\n");
        }


        Files.writeString(
                file,
                csv.toString()
        );
    }
}