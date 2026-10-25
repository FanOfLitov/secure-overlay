package ru.secureoverlay.secure_overlay.experiment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScaleExperimentTest {

    @Test
    void shouldRunThirtyLookupsOnTwelveNodes()
            throws Exception {

        ExperimentRunner runner =
                new ExperimentRunner();


        List<LookupMetric> metrics =
                runner.run(
                        ExperimentTopology.RING,
                        12_000
                );


        /*
         * 30 normal
         * +
         * 30 after failure
         */
        assertEquals(
                60,
                metrics.size()
        );


        long successful =
                metrics.stream()
                        .filter(
                                LookupMetric::success
                        )
                        .count();


        System.out.println(
                "[EXPERIMENT] successful="
                        + successful
                        + "/"
                        + metrics.size()
        );


        /*
         * Не ставим пока 100%.
         *
         * Хотим увидеть реальную
         * эффективность нашего алгоритма.
         */
        assertTrue(
                successful > 0
        );
    }
}