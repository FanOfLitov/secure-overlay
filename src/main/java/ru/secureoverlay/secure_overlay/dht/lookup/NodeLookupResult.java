package ru.secureoverlay.secure_overlay.dht.lookup;

import ru.secureoverlay.secure_overlay.dht.Contact;

import java.util.List;

public record NodeLookupResult(
        Contact found,
        List<Contact> closest,
        int rpcCount,
        int iterations
) {

    public boolean isFound() {
        return found != null;
    }
}