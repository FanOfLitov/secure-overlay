package ru.secureoverlay.secure_overlay.dht.rpc;

import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;

public record StoreRequest(
        DhtRecord record
) {
}