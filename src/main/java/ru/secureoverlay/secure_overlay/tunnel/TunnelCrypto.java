package ru.secureoverlay.secure_overlay.tunnel;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public final class TunnelCrypto {

    private static final int GCM_TAG_BITS = 128;
    private static final int NONCE_LENGTH = 12;

    private TunnelCrypto() {
    }

    public static KeyPair generateEphemeralKeyPair() {

        try {

            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance(
                            "X25519"
                    );

            return generator.generateKeyPair();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot generate X25519 key pair",
                    e
            );
        }
    }


    public static String encodePublicKey(
            PublicKey publicKey
    ) {

        return Base64
                .getEncoder()
                .encodeToString(
                        publicKey.getEncoded()
                );
    }


    public static PublicKey decodePublicKey(
            String encoded
    ) {

        try {

            byte[] bytes =
                    Base64.getDecoder()
                            .decode(encoded);

            KeyFactory factory =
                    KeyFactory.getInstance(
                            "X25519"
                    );

            return factory.generatePublic(
                    new X509EncodedKeySpec(
                            bytes
                    )
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid X25519 public key",
                    e
            );
        }
    }


    public static SecretKey deriveKey(
            KeyPair ownKeyPair,
            PublicKey peerPublicKey,
            UUID tunnelId
    ) {

        try {

            KeyAgreement agreement =
                    KeyAgreement.getInstance(
                            "X25519"
                    );

            agreement.init(
                    ownKeyPair.getPrivate()
            );

            agreement.doPhase(
                    peerPublicKey,
                    true
            );

            byte[] sharedSecret =
                    agreement.generateSecret();


            /*
             * Минимальный HKDF-SHA256:
             * Extract + Expand.
             */
            byte[] salt =
                    new byte[32];

            Mac extractMac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );

            extractMac.init(
                    new SecretKeySpec(
                            salt,
                            "HmacSHA256"
                    )
            );

            byte[] prk =
                    extractMac.doFinal(
                            sharedSecret
                    );


            Mac expandMac =
                    Mac.getInstance(
                            "HmacSHA256"
                    );

            expandMac.init(
                    new SecretKeySpec(
                            prk,
                            "HmacSHA256"
                    )
            );


            byte[] info =
                    (
                            "secure-overlay-tunnel:"
                                    + tunnelId
                    )
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );


            ByteBuffer expandInput =
                    ByteBuffer.allocate(
                            info.length + 1
                    );

            expandInput.put(info);

            /*
             * HKDF block counter = 1
             */
            expandInput.put(
                    (byte) 1
            );


            byte[] keyBytes =
                    expandMac.doFinal(
                            expandInput.array()
                    );


            return new SecretKeySpec(
                    keyBytes,
                    0,
                    32,
                    "AES"
            );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Cannot derive tunnel key",
                    e
            );
        }
    }


    public static EncryptedTunnelPayload encrypt(
            SecretKey key,
            UUID tunnelId,
            byte[] plaintext
    ) {

        try {

            byte[] nonce =
                    new byte[
                            NONCE_LENGTH
                            ];

            new SecureRandom()
                    .nextBytes(nonce);


            Cipher cipher =
                    Cipher.getInstance(
                            "AES/GCM/NoPadding"
                    );


            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(
                            GCM_TAG_BITS,
                            nonce
                    )
            );


            /*
             * tunnelId становится
             * authenticated additional data.
             */
            cipher.updateAAD(
                    tunnelId
                            .toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );


            byte[] ciphertext =
                    cipher.doFinal(
                            plaintext
                    );


            return new EncryptedTunnelPayload(
                    Base64.getEncoder()
                            .encodeToString(nonce),

                    Base64.getEncoder()
                            .encodeToString(
                                    ciphertext
                            )
            );


        } catch (Exception e) {

            throw new IllegalStateException(
                    "Tunnel encryption failed",
                    e
            );
        }
    }


    public static byte[] decrypt(
            SecretKey key,
            UUID tunnelId,
            EncryptedTunnelPayload payload
    ) {

        try {

            byte[] nonce =
                    Base64.getDecoder()
                            .decode(
                                    payload.nonce()
                            );


            byte[] ciphertext =
                    Base64.getDecoder()
                            .decode(
                                    payload.ciphertext()
                            );


            Cipher cipher =
                    Cipher.getInstance(
                            "AES/GCM/NoPadding"
                    );


            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(
                            GCM_TAG_BITS,
                            nonce
                    )
            );


            cipher.updateAAD(
                    tunnelId
                            .toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );


            return cipher.doFinal(
                    ciphertext
            );


        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Tunnel decryption failed",
                    e
            );
        }
    }
}