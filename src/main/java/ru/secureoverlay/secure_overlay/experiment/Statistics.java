package ru.secureoverlay.secure_overlay.experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Statistics {

    private Statistics() {
    }


    public static double mean(
            List<Long> values
    ) {

        return values.stream()
                .mapToLong(
                        Long::longValue
                )
                .average()
                .orElse(0);
    }


    public static long min(
            List<Long> values
    ) {

        return values.stream()
                .mapToLong(
                        Long::longValue
                )
                .min()
                .orElse(0);
    }


    public static long max(
            List<Long> values
    ) {

        return values.stream()
                .mapToLong(
                        Long::longValue
                )
                .max()
                .orElse(0);
    }


    public static double median(
            List<Long> values
    ) {

        if (values.isEmpty()) {
            return 0;
        }


        List<Long> sorted =
                new ArrayList<>(
                        values
                );


        Collections.sort(
                sorted
        );


        int size =
                sorted.size();


        if (size % 2 == 1) {

            return sorted.get(
                    size / 2
            );
        }


        return (
                sorted.get(
                        size / 2 - 1
                )
                        +
                        sorted.get(
                                size / 2
                        )
        )
                / 2.0;
    }


    public static long percentile95(
            List<Long> values
    ) {

        if (values.size() < 20) {

            throw new IllegalArgumentException(
                    "p95 requires at least 20 observations"
            );
        }


        List<Long> sorted =
                new ArrayList<>(
                        values
                );


        Collections.sort(
                sorted
        );


        int index =
                (int)
                        Math.ceil(
                                0.95
                                        * sorted.size()
                        )
                        - 1;


        return sorted.get(
                index
        );
    }
}