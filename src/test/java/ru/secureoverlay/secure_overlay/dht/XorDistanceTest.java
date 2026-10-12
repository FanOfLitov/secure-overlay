package ru.secureoverlay.secure_overlay.dht;

import org.junit.jupiter.api.Test;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XorDistanceTest {

    @Test
    void distanceToSelfShouldBeZero(){

        byte[] value =new byte[NodeId.LENGTH_BYTES];

        value[31] =10;
        NodeId node = new NodeId(value);

        BigInteger distance=XorDistance.between(
                node,
                node
        );

        assertEquals(
                BigInteger.ZERO,
                distance
        );

    }

    @Test
    void distanceShouldBeSymmetric(){
        byte[] a =new byte[NodeId.LENGTH_BYTES];
        byte[] b =new byte[NodeId.LENGTH_BYTES];
        a[31] =10;
        b[31]=20;

        NodeId nodeA =new NodeId(a);
        NodeId nodeB = new NodeId(b);

        assertEquals(XorDistance.between(
                nodeA,
                nodeB
        ),

                XorDistance.between(
                        nodeB,
                        nodeA
                ));
    }


    @Test
    void shouldCalculateKnownDistance() {

        byte[] a =
                new byte[NodeId.LENGTH_BYTES];

        byte[] b =
                new byte[NodeId.LENGTH_BYTES];

        a[31] = 10; // 00001010
        b[31] = 12; // 00001100

        NodeId nodeA =
                new NodeId(a);

        NodeId nodeB =
                new NodeId(b);

        BigInteger distance =
                XorDistance.between(
                        nodeA,
                        nodeB
                );

        /*
         * 00001010 XOR 00001100
         * =
         * 00000110
         * =
         * 6
         */
        assertEquals(
                BigInteger.valueOf(6),
                distance
        );
    }
}

