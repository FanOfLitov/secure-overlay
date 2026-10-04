package ru.secureoverlay.secure_overlay.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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


    @Test
    void encodedFrameSizeShouldIncludePayload() {

        byte[] payload = new byte[100];

        Frame frame = new Frame(
                (byte) 1,
                FrameType.APP_MESSAGE,
                (short) 0,
                UUID.randomUUID(),
                payload
        );

        FrameEncoder encoder = new FrameEncoder();

        byte[] encoded = encoder.encode(frame);

        assertEquals(
                Frame.HEADER_SIZE + 100,
                encoded.length
        );
    }

    @Test
    void shouldRejectPayloadLargerThanMaximum() {

        byte[] payload =
                new byte[Frame.MAX_FRAME_PAYLOAD + 1];

        assertThrows(
                IllegalArgumentException.class,
                () -> new Frame(
                        (byte) 1,
                        FrameType.APP_MESSAGE,
                        (short) 0,
                        UUID.randomUUID(),
                        payload
                )
        );
    }

    @Test
    void shouldAcceptMaximumPayloadSize() {

        byte[] payload =
                new byte[Frame.MAX_FRAME_PAYLOAD];

        Frame frame = new Frame(
                (byte) 1,
                FrameType.APP_MESSAGE,
                (short) 0,
                UUID.randomUUID(),
                payload
        );

        assertEquals(
                Frame.MAX_FRAME_PAYLOAD,
                frame.payloadLength()
        );
    }


    @Test
    void shouldRejectNullFrameType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Frame(
                        (byte) 1,
                        null,
                        (short) 0,
                        UUID.randomUUID(),
                        new byte[0]
                )
        );
    }


    @Test
    void shouldRejectNullRequestId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Frame(
                        (byte) 1,
                        FrameType.PING,
                        (short) 0,
                        null,
                        new byte[0]
                )
        );
    }
    private static class ChunkedInputStream extends InputStream {

        private final ByteArrayInputStream source;
        private final int maxChunkSize;

        private ChunkedInputStream(
                byte[] data,
                int maxChunkSize
        ) {
            this.source = new ByteArrayInputStream(data);
            this.maxChunkSize = maxChunkSize;
        }

        @Override
        public int read() {
            return source.read();
        }

        @Override
        public int read(
                byte[] b,
                int off,
                int len
        ) {

            int limitedLength =
                    Math.min(len, maxChunkSize);

            return source.read(
                    b,
                    off,
                    limitedLength
            );
        }
    }
    @Test
    void shouldDecodeFrameWhenDataArrivesInSmallChunks()
            throws Exception {

        byte[] payload =
                "Hello TCP stream"
                        .getBytes(StandardCharsets.UTF_8);

        Frame original = new Frame(
                (byte) 1,
                FrameType.APP_MESSAGE,
                (short) 0,
                UUID.randomUUID(),
                payload
        );

        FrameEncoder encoder = new FrameEncoder();

        byte[] encoded =
                encoder.encode(original);

        InputStream fragmentedInput =
                new ChunkedInputStream(
                        encoded,
                        3
                );

        FrameDecoder decoder =
                new FrameDecoder();

        Frame decoded =
                decoder.decode(fragmentedInput);

        assertEquals(
                original.requestId(),
                decoded.requestId()
        );

        assertEquals(
                original.type(),
                decoded.type()
        );

        assertArrayEquals(
                original.payload(),
                decoded.payload()
        );
    }

    @Test
    void shouldRejectTruncatedFrame()
            throws Exception {

        Frame original = new Frame(
                (byte) 1,
                FrameType.APP_MESSAGE,
                (short) 0,
                UUID.randomUUID(),
                "HELLO"
                        .getBytes(StandardCharsets.UTF_8)
        );

        FrameEncoder encoder =
                new FrameEncoder();

        byte[] encoded =
                encoder.encode(original);

        byte[] truncated =
                new byte[encoded.length - 2];

        System.arraycopy(
                encoded,
                0,
                truncated,
                0,
                truncated.length
        );

        FrameDecoder decoder =
                new FrameDecoder();

        assertThrows(
                java.io.EOFException.class,
                () -> decoder.decode(
                        new ByteArrayInputStream(truncated)
                )
        );
    }
    @Test
    void shouldRejectDeclaredPayloadLargerThanMaximum()
            throws Exception {

        UUID requestId =
                UUID.randomUUID();

        ByteBuffer header =
                ByteBuffer.allocate(
                        Frame.HEADER_SIZE
                );

        header.order(ByteOrder.BIG_ENDIAN);

        header.put((byte) 1);

        header.put(
                (byte) FrameType.PING.getCode()
        );

        header.putShort((short) 0);

        header.putLong(
                requestId.getMostSignificantBits()
        );

        header.putLong(
                requestId.getLeastSignificantBits()
        );

        /*
         * Объявляем payload на 1 байт больше
         * разрешённого.
         *
         * Сам payload вообще не создаём.
         */
        header.putInt(
                Frame.MAX_FRAME_PAYLOAD + 1
        );

        FrameDecoder decoder =
                new FrameDecoder();

        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode(
                        new ByteArrayInputStream(
                                header.array()
                        )
                )
        );
    }


    @Test
    void shouldRejectNegativePayloadLength()
            throws Exception {

        UUID requestId =
                UUID.randomUUID();

        ByteBuffer header =
                ByteBuffer.allocate(
                        Frame.HEADER_SIZE
                );

        header.order(ByteOrder.BIG_ENDIAN);

        header.put((byte) 1);
        header.put((byte) FrameType.PING.getCode());
        header.putShort((short) 0);

        header.putLong(
                requestId.getMostSignificantBits()
        );

        header.putLong(
                requestId.getLeastSignificantBits()
        );

        header.putInt(-1);

        FrameDecoder decoder =
                new FrameDecoder();

        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode(
                        new ByteArrayInputStream(
                                header.array()
                        )
                )
        );
    }


    @Test
    void shouldRejectUnknownFrameType()
            throws Exception {

        UUID requestId =
                UUID.randomUUID();

        ByteBuffer header =
                ByteBuffer.allocate(
                        Frame.HEADER_SIZE
                );

        header.order(ByteOrder.BIG_ENDIAN);

        header.put((byte) 1);

        /*
         * 99 у нас отсутствует в FrameType.
         */
        header.put((byte) 99);

        header.putShort((short) 0);

        header.putLong(
                requestId.getMostSignificantBits()
        );

        header.putLong(
                requestId.getLeastSignificantBits()
        );

        header.putInt(0);

        FrameDecoder decoder =
                new FrameDecoder();

        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode(
                        new ByteArrayInputStream(
                                header.array()
                        )
                )
        );
    }



}