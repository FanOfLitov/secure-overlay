package ru.secureoverlay.secure_overlay.security.tls;

import ru.secureoverlay.secure_overlay.identity.NodeId;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public final class TlsPeerIdentity {

    private TlsPeerIdentity() {
    }


    public static NodeId nodeId(SSLSession session) throws SSLPeerUnverifiedException {

        Certificate[] certificates = session.getPeerCertificates();


        if (certificates.length == 0) {

            throw new SSLPeerUnverifiedException("Peer has no certificate");
        }


        X509Certificate certificate = (X509Certificate) certificates[0];


        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256"
                    );


            return new NodeId(
                    digest.digest(
                            certificate
                                    .getPublicKey()
                                    .getEncoded()
                    )
            );


        } catch (Exception e) {

            SSLPeerUnverifiedException exception = new SSLPeerUnverifiedException("Cannot calculate peer NodeID");

            exception.initCause(e);

            throw exception;
        }
    }
}