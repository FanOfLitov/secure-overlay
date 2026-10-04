package ru.secureoverlay.secure_overlay.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameStreamTest {

    @Test
    void shouldDecode100FramesFromFragmentedStream()
            throws Exception {

        FrameEncoder encoder = new FrameEncoder();
        FrameDecoder decoder = new FrameDecoder();

        List<Frame> originalFrames =
                new ArrayList<>();

        ByteArrayOutputStream networkStream =
                new ByteArrayOutputStream();

        /*
         * Создаём 100 независимых кадров.
         */
        for (int i = 0; i < 100; i++) {

            byte[] payload =
                    ("message-" + i)
                            .getBytes(StandardCharsets.UTF_8);

            Frame frame = new Frame(
                    (byte) 1,
                    FrameType.APP_MESSAGE,
                    (short) 0,
                    UUID.randomUUID(),
                    payload
            );

            originalFrames.add(frame);

            /*
             * Все кадры записываем подряд,
             * без каких-либо разделителей.
             */
            networkStream.write(
                    encoder.encode(frame)
            );
        }

        byte[] allBytes =
                networkStream.toByteArray();

        /*
         * Имитируем TCP:
         *
         * read() может вернуть случайное
         * количество байтов.
         */
        InputStream fragmentedStream =
                new RandomChunkInputStream(
                        allBytes,
                        12345L
                );

        /*
         * Decoder должен самостоятельно
         * восстановить границы всех кадров.
         */
        for (int i = 0; i < 100; i++) {

            Frame expected =
                    originalFrames.get(i);

            Frame actual =
                    decoder.decode(fragmentedStream);

            assertEquals(
                    expected.version(),
                    actual.version()
            );

            assertEquals(
                    expected.type(),
                    actual.type()
            );

            assertEquals(
                    expected.flags(),
                    actual.flags()
            );

            assertEquals(
                    expected.requestId(),
                    actual.requestId()
            );

            assertArrayEquals(
                    expected.payload(),
                    actual.payload()
            );
        }
    }


    private static class RandomChunkInputStream
            extends InputStream {

        private final ByteArrayInputStream source;
        private final Random random;

        private RandomChunkInputStream(
                byte[] data,
                long seed
        ) {

            this.source =
                    new ByteArrayInputStream(data);

            this.random =
                    new Random(seed);
        }

        @Override
        public int read() {

            return source.read();
        }

        @Override
        public int read(
                byte[] buffer,
                int offset,
                int length
        ) {

            if (length == 0) {
                return 0;
            }

            /*
             * TCP может вернуть меньше байтов,
             * чем запросило приложение.
             *
             * Здесь искусственно возвращаем
             * от 1 до length байтов.
             */
            int chunkSize =
                    1 + random.nextInt(length);

            return source.read(
                    buffer,
                    offset,
                    chunkSize
            );
        }
    }
}