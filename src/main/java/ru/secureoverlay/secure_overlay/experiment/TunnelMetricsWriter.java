package ru.secureoverlay.secure_overlay.experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TunnelMetricsWriter {

    private TunnelMetricsWriter() {
    }


    public static void write(
            Path path,
            List<TunnelMetric> metrics
    ) throws IOException {

        if (path.getParent() != null) {

            Files.createDirectories(
                    path.getParent()
            );
        }


        StringBuilder csv =
                new StringBuilder();


        csv.append(
                "run,"
                        + "scenario,"
                        + "delivered,"
                        + "build_time_ms,"
                        + "end_to_end_latency_ms,"
                        + "recovery_time_ms\n"
        );


        for (TunnelMetric metric : metrics) {

            csv.append(
                    metric.run()
            );

            csv.append(",");

            csv.append(
                    metric.scenario()
            );

            csv.append(",");

            csv.append(
                    metric.delivered()
            );

            csv.append(",");

            csv.append(
                    metric.buildTimeMs()
            );

            csv.append(",");

            csv.append(
                    metric.endToEndLatencyMs()
            );

            csv.append(",");

            csv.append(
                    metric.recoveryTimeMs()
            );

            csv.append("\n");
        }


        Files.writeString(
                path,
                csv.toString()
        );
    }
}