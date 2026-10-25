package ru.secureoverlay.secure_overlay.experiment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsTest {

    @Test
    void shouldCalculateStatistics() {

        List<Long> values =
                List.of(
                        10L,
                        20L,
                        30L,
                        40L,
                        50L
                );


        assertEquals(
                30.0,
                Statistics.mean(
                        values
                )
        );


        assertEquals(
                30.0,
                Statistics.median(
                        values
                )
        );


        assertEquals(
                10,
                Statistics.min(
                        values
                )
        );


        assertEquals(
                50,
                Statistics.max(
                        values
                )
        );
    }


    @Test
    void shouldCalculateP95() {

        List<Long> values =
                java.util.stream
                        .LongStream
                        .rangeClosed(
                                1,
                                100
                        )
                        .boxed()
                        .toList();


        assertEquals(
                95,
                Statistics.percentile95(
                        values
                )
        );
    }


    @Test
    void shouldRejectP95ForSmallSample() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Statistics
                                .percentile95(
                                        List.of(
                                                1L,
                                                2L
                                        )
                                )
        );
    }
}