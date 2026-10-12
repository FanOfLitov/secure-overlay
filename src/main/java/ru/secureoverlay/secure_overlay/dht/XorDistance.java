package ru.secureoverlay.secure_overlay.dht;


import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.math.BigInteger;

public class XorDistance {
    private XorDistance(){}

    public static BigInteger between(
            NodeId first,
            NodeId second
    ){
        byte[] a = first.bytes();
        byte[] b = second.bytes();

        byte[] result=new byte[NodeId.LENGTH_BYTES];

        for (int i=0;i<result.length;i++){
            result[i] =(byte)(a[i] ^b[i]);
        }

        return new BigInteger(1, result);
    }
}
