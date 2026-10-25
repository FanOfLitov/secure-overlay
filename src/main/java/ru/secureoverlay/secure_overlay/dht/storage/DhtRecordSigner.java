package ru.secureoverlay.secure_overlay.dht.storage;

import ru.secureoverlay.secure_overlay.identity.NodeIdentity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class DhtRecordSigner {

    private DhtRecordSigner() {
    }


    public static DhtRecord sign(
            String key,
            String value,
            long version,
            long expiresAtEpochMs,
            NodeIdentity identity
    ) {

        try {

            String publicKey =
                    Base64.getEncoder()
                            .encodeToString(
                                    identity
                                            .keyPair()
                                            .getPublic()
                                            .getEncoded()
                            );

            byte[] data =
                    canonicalBytes(
                            key,
                            value,
                            version,
                            expiresAtEpochMs,
                            publicKey
                    );

            Signature signer =
                    Signature.getInstance(
                            "Ed25519"
                    );

            signer.initSign(
                    identity
                            .keyPair()
                            .getPrivate()
            );

            signer.update(data);

            String signature =
                    Base64.getEncoder()
                            .encodeToString(
                                    signer.sign()
                            );

            return new DhtRecord(
                    key,
                    value,
                    version,
                    expiresAtEpochMs,
                    publicKey,
                    signature
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to sign DHT record",
                    e
            );
        }
    }


    public static boolean verify(
            DhtRecord record
    ) {

        try {

            byte[] publicKeyBytes =
                    Base64.getDecoder().decode(record.ownerPublicKey());

            KeyFactory keyFactory =
                    KeyFactory.getInstance("Ed25519");

            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            byte[] data =
                    canonicalBytes(
                            record.key(),
                            record.value(),
                            record.version(),
                            record.expiresAtEpochMs(),
                            record.ownerPublicKey()
                    );

            Signature verifier = Signature.getInstance("Ed25519");

            verifier.initVerify(publicKey);

            verifier.update(data);

            return verifier.verify(
                    Base64.getDecoder()
                            .decode(
                                    record.signature()
                            )
            );

        } catch (Exception e) {

            return false;
        }
    }


    private static byte[] canonicalBytes(
            String key,
            String value,
            long version,
            long expiresAtEpochMs,
            String publicKey
    ) throws Exception {

        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        try (DataOutputStream output =
                     new DataOutputStream(bytes)) {

            writeString(
                    output,
                    key
            );

            writeString(
                    output,
                    value
            );

            output.writeLong(version);

            output.writeLong(
                    expiresAtEpochMs
            );

            writeString(
                    output,
                    publicKey
            );
        }

        return bytes.toByteArray();
    }


    private static void writeString(
            DataOutputStream output,
            String value
    ) throws Exception {

        byte[] data =
                value.getBytes(
                        StandardCharsets.UTF_8
                );

        output.writeInt(data.length);

        output.write(data);
    }
}