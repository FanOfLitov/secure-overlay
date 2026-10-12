package ru.secureoverlay.secure_overlay.identity;

import java.security.KeyPair;

public record NodeIdentity(
        NodeId nodeId,
        KeyPair keyPair
) {

    public NodeIdentity{
        if(nodeId==null){
            throw new IllegalArgumentException("NodeId cannot be null");
        }
        if(keyPair==null){
            throw new IllegalArgumentException("KeyPair cannot be null");
        }
    }
}
