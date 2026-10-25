package ru.secureoverlay.secure_overlay.dht.rpc;

import java.util.List;
public record FindNodeResponse (
        List<ContactDto> contacts

){
}
