package ru.secureoverlay.secure_overlay.protocol;

import java.util.UUID;

public record Frame(
        byte version,
        FrameType type,
        short flags,
        UUID requestId,
        byte[] payload
) {
    public static final int HEADER_SIZE =24;
    public static final int MAX_FRAME_PAYLOAD =65_536;

    public Frame{

        if(type==null){
            throw new IllegalArgumentException(
                    "Frame type cannot be null"
            );
        }
        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Request ID cannot be null"
            );
        }

        if(payload ==null){
            payload = new byte[0];
        }

        if(payload.length > MAX_FRAME_PAYLOAD) throw new IllegalArgumentException("Payload too large: "+"payload.length");


    }
    public int payloadLength(){
        return payload.length;
    }

}

