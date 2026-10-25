package ru.secureoverlay.secure_overlay.dht.rpc;


import ru.secureoverlay.secure_overlay.dht.Contact;
import ru.secureoverlay.secure_overlay.identity.NodeId;

import java.time.Instant;

public class ContactMapper {

    private ContactMapper(){}

    public static ContactDto toDto(Contact contact){
        return new ContactDto(
                contact.nodeId().toHex(),
                contact.host(),
                contact.port()
        );
    }

    public static Contact fromDto(ContactDto dto){
        return new Contact(NodeId.fromHex(dto.nodeId()),
                dto.host(),
                dto.port(),
                Instant.now());
    }
}
