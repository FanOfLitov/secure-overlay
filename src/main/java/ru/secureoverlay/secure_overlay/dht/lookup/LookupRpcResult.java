package ru.secureoverlay.secure_overlay.dht.lookup;

import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.dht.rpc.FindNodeResponse;

public record LookupRpcResult(
        Contact queriedContact,
        FindNodeResponse response,
        Exception error
) {

    public boolean successful() {
        return error == null && response != null;
    }
}