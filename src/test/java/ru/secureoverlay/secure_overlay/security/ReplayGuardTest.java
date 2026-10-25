package ru.secureoverlay.secure_overlay.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReplayGuardTest {

    @Test
    void shouldRejectSameRequestIdTwice() {

        ReplayGuard guard =
                new ReplayGuard(
                        Duration.ofMinutes(5)
                );


        UUID requestId =
                UUID.randomUUID();


        assertTrue(
                guard.accept(
                        requestId
                )
        );


        assertFalse(
                guard.accept(
                        requestId
                )
        );
    }


    @Test
    void shouldAcceptDifferentRequestIds() {

        ReplayGuard guard =
                new ReplayGuard(
                        Duration.ofMinutes(5)
                );


        assertTrue(
                guard.accept(
                        UUID.randomUUID()
                )
        );


        assertTrue(
                guard.accept(
                        UUID.randomUUID()
                )
        );
    }
}