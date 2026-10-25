package ru.secureoverlay.secure_overlay.transport;

import ru.secureoverlay.secure_overlay.dht.rpc.*;
import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.io.IOException;

public interface DhtRpc {

    boolean ping(
            String host,
            int port
    ) throws IOException;


    FindNodeResponse findNode(
            String host,
            int port,
            NodeId target
    ) throws IOException;


    StoreResponse store(
            String host,
            int port,
            DhtRecord record
    ) throws IOException;


    FindValueResponse findValue(
            String host,
            int port,
            String key
    ) throws IOException;
}