package ru.secureoverlay.secure_overlay.protocol;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FrameTypeTest {

    @Test
    void protocolCodesMustRemainStable() {

        assertEquals(1, FrameType.PING.getCode());
        assertEquals(2, FrameType.PONG.getCode());

        assertEquals(
                3,
                FrameType.FIND_NODE_REQUEST.getCode()
        );

        assertEquals(
                4,
                FrameType.FIND_NODE_RESPONSE.getCode()
        );

        assertEquals(
                5,
                FrameType.STORE_REQUEST.getCode()
        );

        assertEquals(
                6,
                FrameType.STORE_RESPONSE.getCode()
        );

        assertEquals(
                7,
                FrameType.FIND_VALUE_REQUEST.getCode()
        );

        assertEquals(
                8,
                FrameType.FIND_VALUE_RESPONSE.getCode()
        );

        assertEquals(
                9,
                FrameType.TUNNEL_BUILD.getCode()
        );

        assertEquals(
                10,
                FrameType.TUNNEL_BUILD_OK.getCode()
        );

        assertEquals(
                11,
                FrameType.TUNNEL_BUILD_FAIL.getCode()
        );

        assertEquals(
                12,
                FrameType.TUNNEL_DATA.getCode()
        );

        assertEquals(
                13,
                FrameType.TUNNEL_ACK.getCode()
        );

        assertEquals(
                14,
                FrameType.TUNNEL_CLOSE.getCode()
        );

        assertEquals(
                15,
                FrameType.APP_MESSAGE.getCode()
        );

        assertEquals(
                16,
                FrameType.APP_ACK.getCode()
        );

        assertEquals(
                17,
                FrameType.ERROR.getCode()
        );
    }


    @Test
    void protocolCodesMustBeUnique() {

        Set<Integer> codes =
                new HashSet<>();


        for (FrameType type :
                FrameType.values()) {

            assertTrue(
                    codes.add(
                            type.getCode()
                    )
            );
        }
    }


    @Test
    void everyCodeMustDecodeBackToSameType() {

        for (FrameType type :
                FrameType.values()) {

            assertSame(
                    type,
                    FrameType.fromCode(
                            type.getCode()
                    )
            );
        }
    }
}