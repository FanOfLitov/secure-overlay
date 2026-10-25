package ru.secureoverlay.secure_overlay.dht.rpc;

import ru.secureoverlay.secure_overlay.dht.storage.DhtRecord;

import java.util.List;

public record FindValueResponse(

        boolean found,

        DhtRecord record,

        List<ContactDto> contacts

) {
}