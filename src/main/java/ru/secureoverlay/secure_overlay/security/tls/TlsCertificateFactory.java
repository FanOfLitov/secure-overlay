
package ru.secureoverlay.secure_overlay.security.tls;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import ru.secureoverlay.secure_overlay.identity.NodeIdentity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class TlsCertificateFactory {

    private TlsCertificateFactory() {
    }


    public static X509Certificate create(NodeIdentity identity) {

        try {

            X500Name subject = new X500Name("CN=" + identity.nodeId().toHex());


            Instant now = Instant.now();


            Date notBefore = Date.from(now.minus(1, ChronoUnit.MINUTES)
                    );


            Date notAfter = Date.from(now.plus(
                                    365,
                                    ChronoUnit.DAYS
                            )
                    );


            BigInteger serial = new BigInteger(160, new SecureRandom()).abs();


            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                            subject,
                            serial,
                            notBefore,
                            notAfter,
                            subject,
                            identity
                                    .keyPair()
                                    .getPublic()
                    );


            ContentSigner signer =
                    new JcaContentSignerBuilder(
                            "Ed25519"
                    )
                            .build(
                                    identity
                                            .keyPair()
                                            .getPrivate()
                            );


            X509CertificateHolder holder = builder.build(signer);


            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);


            certificate.checkValidity();

            certificate.verify(identity
                            .keyPair()
                            .getPublic()
            );


            return certificate;


        } catch (Exception e) {

            throw new IllegalStateException("Failed to create TLS certificate", e);
        }
    }
}