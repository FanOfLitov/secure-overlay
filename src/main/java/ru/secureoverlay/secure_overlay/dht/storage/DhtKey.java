package ru.secureoverlay.secure_overlay.dht.storage;

import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class DhtKey {
    private DhtKey(){}

    public static NodeId fromString(String value){
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException("Key cannot be blank");
        }

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return new NodeId(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 not available",
                    e
            );
        }
    }
}