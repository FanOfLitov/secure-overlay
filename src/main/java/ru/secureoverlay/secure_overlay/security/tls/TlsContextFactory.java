package ru.secureoverlay.secure_overlay.security.tls;

import ru.secureoverlay.secure_overlay.identity.NodeIdentity;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public final class TlsContextFactory {

    private static final char[] PASSWORD = "secure-overlay".toCharArray();


    private TlsContextFactory() {
    }


    public static SSLContext create(NodeIdentity identity
    ) {

        try {X509Certificate certificate = TlsCertificateFactory.create(identity);


            KeyStore keyStore = KeyStore.getInstance("PKCS12");


            keyStore.load(null, PASSWORD);


            keyStore.setKeyEntry(
                    "node",
                    identity
                            .keyPair()
                            .getPrivate(),
                    PASSWORD,
                    new java.security.cert.Certificate[]{
                            certificate
                    }
            );


            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());


            keyManagerFactory.init(
                    keyStore,
                    PASSWORD
            );


            TrustManager[] trustManagers =
                    new TrustManager[]{new SelfSignedPeerTrustManager()};


            SSLContext context =
                    SSLContext.getInstance(
                            "TLS"
                    );


            context.init(
                    keyManagerFactory.getKeyManagers(),
                    trustManagers,
                    new SecureRandom()
            );


            return context;


        } catch (Exception e) {

            throw new IllegalStateException("Failed to initialize TLS context", e
            );
        }
    }
}