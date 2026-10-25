package ru.secureoverlay.secure_overlay.tunnel;

import ru.secureoverlay.secure_overlay.dht.Contact;

import javax.crypto.SecretKey;
import java.util.UUID;

public record BuiltTunnel(

        UUID tunnelId,

        Contact entryRelay,

        SecretKey key

) {
}