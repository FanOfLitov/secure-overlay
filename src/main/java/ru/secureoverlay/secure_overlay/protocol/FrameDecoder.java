package ru.secureoverlay.secure_overlay.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class FrameDecoder {

    public Frame decode(InputStream input)
            throws IOException {

        byte[] header =
                readExactly(
                        input,
                        Frame.HEADER_SIZE
                );

        ByteBuffer buffer =
                ByteBuffer.wrap(header);

        buffer.order(ByteOrder.BIG_ENDIAN);

        byte version =
                buffer.get();

        int typeCode =
                Byte.toUnsignedInt(
                        buffer.get()
                );

        short flags =
                buffer.getShort();

        UUID requestId =
                readUuid(buffer);

        int payloadLength =
                buffer.getInt();

        validatePayloadLength(
                payloadLength
        );

        byte[] payload =
                readExactly(
                        input,
                        payloadLength
                );

        FrameType type =
                FrameType.fromCode(typeCode);

        return new Frame(
                version,
                type,
                flags,
                requestId,
                payload
        );
    }

    private void validatePayloadLength(
            int payloadLength
    ) {

        if (payloadLength < 0) {

            throw new IllegalArgumentException(
                    "Negative payload length: "
                            + payloadLength
            );
        }

        if (payloadLength >
                Frame.MAX_FRAME_PAYLOAD) {

            throw new IllegalArgumentException(
                    "Payload length exceeds limit: "
                            + payloadLength
            );
        }
    }

    private byte[] readExactly(
            InputStream input,
            int length
    ) throws IOException {

        byte[] data =
                new byte[length];

        int offset = 0;

        while (offset < length) {

            int read =
                    input.read(
                            data,
                            offset,
                            length - offset
                    );

            if (read == -1) {

                throw new EOFException(
                        "Connection closed while reading frame"
                );
            }

            offset += read;
        }

        return data;
    }

    private UUID readUuid(
            ByteBuffer buffer
    ) {

        long mostSignificantBits =
                buffer.getLong();

        long leastSignificantBits =
                buffer.getLong();

        return new UUID(
                mostSignificantBits,
                leastSignificantBits
        );
    }
}