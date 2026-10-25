package ru.secureoverlay.secure_overlay.protocol;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonPayloadCodecTest {

    @Test
    void shouldEncodeAndDecodeFindNodeRequest() {

        JsonPayloadCodec codec =
                new JsonPayloadCodec();

        FindNodeRequest original =
                new FindNodeRequest(
                        "abcdef"
                );

        byte[] payload =
                codec.encode(original);

        FindNodeRequest decoded =
                codec.decode(
                        payload,
                        FindNodeRequest.class
                );

        assertEquals(
                original,
                decoded
        );
    }
}