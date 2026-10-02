package ru.secureoverlay.secure_overlay.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class FrameEncoder {

    public byte[] encode(Frame frame){
        int totalSize =Frame.HEADER_SIZE+frame.payloadLength();

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(frame.version());
        buffer.put((byte)frame.type().getCode());
        buffer.putShort(frame.flags());

        putUuid(buffer, frame.requestId());

        buffer.putInt(frame.payloadLength());

        buffer.put(frame.payload());
        return buffer.array();
    }

    private void putUuid(ByteBuffer buffer, UUID uuid){
       buffer.putLong(uuid.getMostSignificantBits());

       buffer.putLong(uuid.getLeastSignificantBits());
    }
}
