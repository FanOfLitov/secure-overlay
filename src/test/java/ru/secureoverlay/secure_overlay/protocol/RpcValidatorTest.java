package ru.secureoverlay.secure_overlay.protocol;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RpcValidatorTest {

    @Test
    void shouldAcceptMatchingRequestId() {

        UUID requestId =
                UUID.randomUUID();

        Frame request =
                new Frame(
                        (byte) 1,
                        FrameType.PING,
                        (short) 0,
                        requestId,
                        new byte[0]
                );

        Frame response =
                new Frame(
                        (byte) 1,
                        FrameType.PONG,
                        (short) 0,
                        requestId,
                        new byte[0]
                );

        assertDoesNotThrow(
                () -> RpcValidator.validateResponse(
                        request,
                        response
                )
        );
    }


    @Test
    void shouldRejectDifferentRequestId() {

        Frame request =
                new Frame(
                        (byte) 1,
                        FrameType.PING,
                        (short) 0,
                        UUID.randomUUID(),
                        new byte[0]
                );

        Frame response =
                new Frame(
                        (byte) 1,
                        FrameType.PONG,
                        (short) 0,

                        // другой UUID
                        UUID.randomUUID(),

                        new byte[0]
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> RpcValidator.validateResponse(
                        request,
                        response
                )
        );
    }
}