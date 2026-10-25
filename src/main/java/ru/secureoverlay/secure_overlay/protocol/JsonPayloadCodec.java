package ru.secureoverlay.secure_overlay.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPayloadCodec {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] encode(Object value) {

        try {
            return objectMapper.writeValueAsBytes(value);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to encode payload", e
            );
        }
    }

    public <T> T decode(
            byte[] payload,
            Class<T> type
    ) {

        try {
            return objectMapper.readValue(payload,type);

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Failed to decode payload",e

            );
        }
    }
}