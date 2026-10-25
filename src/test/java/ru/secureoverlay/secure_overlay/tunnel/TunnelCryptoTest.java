package ru.secureoverlay.secure_overlay.tunnel;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TunnelCryptoTest {

    @Test
    void bothEndpointsShouldDeriveSameKey() {

        UUID tunnelId =
                UUID.randomUUID();


        KeyPair initiator =
                TunnelCrypto
                        .generateEphemeralKeyPair();


        KeyPair destination =
                TunnelCrypto
                        .generateEphemeralKeyPair();


        SecretKey initiatorKey =
                TunnelCrypto.deriveKey(
                        initiator,
                        destination.getPublic(),
                        tunnelId
                );


        SecretKey destinationKey =
                TunnelCrypto.deriveKey(
                        destination,
                        initiator.getPublic(),
                        tunnelId
                );


        assertArrayEquals(
                initiatorKey.getEncoded(),
                destinationKey.getEncoded()
        );
    }


    @Test
    void shouldEncryptAndDecryptPayload() {

        UUID tunnelId =
                UUID.randomUUID();


        KeyPair a =
                TunnelCrypto
                        .generateEphemeralKeyPair();


        KeyPair b =
                TunnelCrypto
                        .generateEphemeralKeyPair();


        SecretKey aKey =
                TunnelCrypto.deriveKey(
                        a,
                        b.getPublic(),
                        tunnelId
                );


        SecretKey bKey =
                TunnelCrypto.deriveKey(
                        b,
                        a.getPublic(),
                        tunnelId
                );


        byte[] plaintext =
                "HELLO-TUNNEL"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );


        EncryptedTunnelPayload encrypted =
                TunnelCrypto.encrypt(
                        aKey,
                        tunnelId,
                        plaintext
                );


        byte[] decrypted =
                TunnelCrypto.decrypt(
                        bKey,
                        tunnelId,
                        encrypted
                );


        assertArrayEquals(
                plaintext,
                decrypted
        );


        /*
         * И ciphertext не равен plaintext.
         */
        assertNotEquals(
                "HELLO-TUNNEL",
                encrypted.ciphertext()
        );
    }
}