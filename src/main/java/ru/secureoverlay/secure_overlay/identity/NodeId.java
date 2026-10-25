package ru.secureoverlay.secure_overlay.identity;

import java.util.Arrays;
import java.util.HexFormat;

public class NodeId {

    public static final int LENGTH_BYTES =32;

    private final byte[] value;

    public NodeId(byte[] value) {
        if (value == null) throw new IllegalArgumentException("NodeId cannot be null");

        if (value.length != LENGTH_BYTES) {
            throw new IllegalArgumentException("NodeId must be 32 bytes");
        }

        this.value = Arrays.copyOf(
                value,
                value.length
        );
    }

    public byte[] bytes(){
        return Arrays.copyOf(
                value,
                value.length
        );
        }

    public String toHex(){
        return HexFormat.of().formatHex(value);
    }

    @Override
    public String toString(){
        return toHex();
    }

    @Override
    public boolean equals(Object object){
        if(this==object){
            return true;
        }
        if(!(object instanceof NodeId other)){
            return false;
        }

        return Arrays.equals(
                value,
                other.value
        );
    }

    @Override
    public int hashCode(){

            return Arrays.hashCode(value);
        }

    public static NodeId fromHex(String hex){
        if(hex==null){
            throw new IllegalArgumentException(
                    "NodeId hex cannot be null"
            );
        }

        byte[] bytes;

        try{
            bytes =HexFormat.of().parseHex(hex);
        } catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid NodeId hex", e);
        }
        return new NodeId(bytes);
    }
    }






