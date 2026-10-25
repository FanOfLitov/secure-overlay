package ru.secureoverlay.secure_overlay.security.tls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.secureoverlay.secure_overlay.identity.IdentityService;
import ru.secureoverlay.secure_overlay.identity.NodeId;
import ru.secureoverlay.secure_overlay.identity.NodeIdentity;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TlsCertificateFactoryTest {

    @TempDir
    Path tempDir;


    @Test
    void certificateMustContainIdentityPublicKey()
            throws Exception {

        NodeIdentity identity =
                new IdentityService()
                        .loadOrCreate(
                                tempDir
                        );


        X509Certificate certificate =
                TlsCertificateFactory.create(
                        identity
                );


        byte[] hash =
                MessageDigest
                        .getInstance(
                                "SHA-256"
                        )
                        .digest(
                                certificate
                                        .getPublicKey()
                                        .getEncoded()
                        );


        NodeId certificateNodeId =
                new NodeId(hash);


        assertEquals(
                identity.nodeId(),
                certificateNodeId
        );
    }
}