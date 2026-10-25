package ru.secureoverlay.secure_overlay.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OverlayPropertiesTest {

    @Test
    void shouldContainCourseworkDefaults() {

        OverlayProperties properties =
                new OverlayProperties();


        assertEquals(
                256,
                properties.getNodeIdBits()
        );


        assertEquals(
                3,
                properties.getKBucketSize()
        );


        assertEquals(
                3,
                properties.getAlpha()
        );


        assertEquals(
                3,
                properties.getReplicationFactor()
        );


        assertEquals(
                2,
                properties.getTunnelRelays()
        );


        assertEquals(
                5000,
                properties.getSocketTimeoutMs()
        );
    }
}