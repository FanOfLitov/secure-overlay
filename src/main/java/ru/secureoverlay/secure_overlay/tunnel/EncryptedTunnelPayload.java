package ru.secureoverlay.secure_overlay.tunnel;

public record EncryptedTunnelPayload(
        String nonce,
        String ciphertext
) {
}