package ru.secureoverlay.secure_overlay.security.tls;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SelfSignedPeerTrustManager
        implements X509TrustManager {


    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        validate(chain);
    }


    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        validate(chain);
    }


    private void validate(X509Certificate[] chain) throws CertificateException {

        if (chain == null || chain.length == 0) {throw new CertificateException("Peer certificate is missing");
        }


        X509Certificate certificate = chain[0];


        certificate.checkValidity();


        try {

            /*
             * Для нашей P2P-модели сертификат
             * self-signed.
             */
            certificate.verify(certificate.getPublicKey());

        } catch (Exception e) {

            throw new CertificateException("Invalid peer certificate", e);
        }
    }


    @Override
    public X509Certificate[]
    getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}