package org.openpdf.text.pdf.sign;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Self-signed RSA identities for signature tests, generated once per JVM and name.
 */
public final class TestSigningKeys {

    private static final long ONE_DAY_MILLIS = 24L * 60 * 60 * 1000;
    private static final Map<String, TestSigningKeys> CACHE = new ConcurrentHashMap<>();

    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    private TestSigningKeys(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    /**
     * Returns the identity with the given common name, creating it on first use.
     *
     * @param commonName the common name of the certificate subject
     * @return the identity
     */
    public static TestSigningKeys forName(String commonName) {
        return CACHE.computeIfAbsent(commonName, TestSigningKeys::create);
    }

    private static TestSigningKeys create(String commonName) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            X500Name subject = new X500Name("CN=" + commonName + ", O=OpenPDF Test");
            long now = System.currentTimeMillis();
            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(subject,
                    BigInteger.valueOf(now), new Date(now - ONE_DAY_MILLIS), new Date(now + 365 * ONE_DAY_MILLIS),
                    subject, keyPair.getPublic());
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(builder.build(signer));
            return new TestSigningKeys(keyPair.getPrivate(), certificate);
        } catch (GeneralSecurityException | OperatorCreationException e) {
            throw new IllegalStateException("Cannot create the test identity " + commonName, e);
        }
    }

    /**
     * Returns the private key.
     *
     * @return the private key
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns the certificate chain, made of the self-signed certificate only.
     *
     * @return the chain
     */
    public Certificate[] getChain() {
        return new Certificate[]{certificate};
    }
}
