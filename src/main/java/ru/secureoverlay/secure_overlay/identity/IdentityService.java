package ru.secureoverlay.secure_overlay.identity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class IdentityService {

    private static final String KEY_ALGORITHM =
            "Ed25519";

    private static final String HASH_ALGORITHM =
            "SHA-256";

    private static final String PRIVATE_KEY_FILE =
            "identity-private.key";

    private static final String PUBLIC_KEY_FILE =
            "identity-public.key";


    public NodeIdentity loadOrCreate(
            Path stateDirectory
    ) {

        try {

            Files.createDirectories(
                    stateDirectory
            );

            Path privateKeyPath =
                    stateDirectory.resolve(
                            PRIVATE_KEY_FILE
                    );

            Path publicKeyPath =
                    stateDirectory.resolve(
                            PUBLIC_KEY_FILE
                    );

            KeyPair keyPair;

            if (Files.exists(privateKeyPath)
                    && Files.exists(publicKeyPath)) {

                keyPair =
                        loadKeyPair(
                                privateKeyPath,
                                publicKeyPath
                        );

            } else {

                keyPair =
                        generateKeyPair();

                saveKeyPair(
                        keyPair,
                        privateKeyPath,
                        publicKeyPath
                );
            }

            NodeId nodeId =
                    calculateNodeId(
                            keyPair.getPublic()
                    );

            return new NodeIdentity(
                    nodeId,
                    keyPair
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to load/create node identity",
                    e
            );
        }
    }


    public NodeId calculateNodeId(
            PublicKey publicKey
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            HASH_ALGORITHM
                    );

            byte[] hash =
                    digest.digest(
                            publicKey.getEncoded()
                    );

            return new NodeId(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to calculate NodeId",
                    e
            );
        }
    }


    public boolean matches(
            NodeId claimedNodeId,
            PublicKey publicKey
    ) {

        NodeId calculated =
                calculateNodeId(publicKey);

        return calculated.equals(
                claimedNodeId
        );
    }


    private KeyPair generateKeyPair()
            throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance(
                        KEY_ALGORITHM
                );

        return generator.generateKeyPair();
    }


    private void saveKeyPair(
            KeyPair keyPair,
            Path privateKeyPath,
            Path publicKeyPath
    ) throws IOException {

        Files.write(
                privateKeyPath,
                keyPair.getPrivate()
                        .getEncoded()
        );

        Files.write(
                publicKeyPath,
                keyPair.getPublic()
                        .getEncoded()
        );
    }


    private KeyPair loadKeyPair(
            Path privateKeyPath,
            Path publicKeyPath
    ) throws Exception {

        byte[] privateBytes =
                Files.readAllBytes(
                        privateKeyPath
                );

        byte[] publicBytes =
                Files.readAllBytes(
                        publicKeyPath
                );

        KeyFactory factory =
                KeyFactory.getInstance(
                        KEY_ALGORITHM
                );

        PrivateKey privateKey =
                factory.generatePrivate(
                        new PKCS8EncodedKeySpec(
                                privateBytes
                        )
                );

        PublicKey publicKey =
                factory.generatePublic(
                        new X509EncodedKeySpec(
                                publicBytes
                        )
                );

        return new KeyPair(
                publicKey,
                privateKey
        );
    }
}

