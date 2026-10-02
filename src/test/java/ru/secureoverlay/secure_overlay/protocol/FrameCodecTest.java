package ru.secureoverlay.secure_overlay.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FrameCodecTest {

    @Test
    void shouldEncodeAndDecodeFrame()
            throws Exception {

        UUID requestId =
                UUID.randomUUID();

        byte[] payload =
                "HELLO".getBytes(
                        StandardCharsets.UTF_8
                );

        Frame original =
                new Frame(
                        (byte) 1,
                        FrameType.PING,
                        (short) 0,
                        requestId,
                        payload
                );

        FrameEncoder encoder =
                new FrameEncoder();

        byte[] encoded =
                encoder.encode(original);

        FrameDecoder decoder =
                new FrameDecoder();

        Frame decoded =
                decoder.decode(
                        new ByteArrayInputStream(encoded)
                );

        assertEquals(
                original.version(),
                decoded.version()
        );

        assertEquals(
                original.type(),
                decoded.type()
        );

        assertEquals(
                original.flags(),
                decoded.flags()
        );

        assertEquals(
                original.requestId(),
                decoded.requestId()
        );

        assertArrayEquals(
                original.payload(),
                decoded.payload()
        );
    }

    @Test
    void headerShouldBeExactly24Bytes() {

        Frame frame =
                new Frame(
                        (byte) 1,
                        FrameType.PING,
                        (short) 0,
                        UUID.randomUUID(),
                        new byte[0]
                );

        FrameEncoder encoder =
                new FrameEncoder();

        byte[] encoded =
                encoder.encode(frame);

        assertEquals(
                24,
                encoded.length
        );
    }
    @Test
    void shouldRejectTooLargePayload() {

        byte[] payload =
                new byte[
                        Frame.MAX_FRAME_PAYLOAD + 1
                        ];

        assertThrows(
                IllegalArgumentException.class,
                () -> new Frame(
                        (byte) 1,
                        FrameType.PING,
                        (short) 0,
                        UUID.randomUUID(),
                        payload
                )
        );
    }
}