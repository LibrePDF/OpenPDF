package org.openpdf.text.pdf;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The OCSP client only returns a response that answers its request and is signed by the issuer of the certificate.
 * Both clients only talk to http and https URLs and do not read responses of unlimited size.
 */
@Timeout(60)
class OcspAndTsaClientTest {

    private static HttpServer server;
    private static String baseUrl;
    private static KeyPair issuerKeys;
    private static KeyPair otherKeys;
    private static X509Certificate issuer;
    private static X509Certificate checked;
    private static volatile byte[] ocspBody;

    @BeforeAll
    static void start() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        issuerKeys = generator.generateKeyPair();
        otherKeys = generator.generateKeyPair();
        KeyPair checkedKeys = generator.generateKeyPair();
        issuer = issue("CN=Test Issuer", issuerKeys.getPublic(), "CN=Test Issuer", issuerKeys.getPrivate(), 1, true);
        checked = issue("CN=Checked", checkedKeys.getPublic(), "CN=Test Issuer", issuerKeys.getPrivate(), 42, false);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ocsp", exchange -> respond(exchange, ocspBody));
        server.createContext("/large", exchange -> respond(exchange, new byte[2 * 1024 * 1024]));
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stop() {
        server.stop(0);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, byte[] body) throws IOException {
        exchange.getRequestBody().readAllBytes();
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static X509Certificate issue(String subject, PublicKey key, String issuerName, PrivateKey issuerKey,
            int serial, boolean authority) throws Exception {
        Date from = new Date(System.currentTimeMillis() - 86_400_000L);
        Date to = new Date(System.currentTimeMillis() + 86_400_000L);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(new X500Name(issuerName),
                BigInteger.valueOf(serial), from, to, new X500Name(subject), key);
        if (authority) {
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        }
        return new JcaX509CertificateConverter().getCertificate(
                builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey)));
    }

    /**
     * Builds a response with the status "good" for a serial number, signed with the given key.
     */
    private static byte[] ocspResponse(int serial, KeyPair signer) throws Exception {
        DigestCalculatorProvider digests = new JcaDigestCalculatorProviderBuilder().build();
        CertificateID id = new CertificateID(digests.get(CertificateID.HASH_SHA1),
                new JcaX509CertificateHolder(issuer), BigInteger.valueOf(serial));
        BasicOCSPRespBuilder builder = new BasicOCSPRespBuilder(
                SubjectPublicKeyInfo.getInstance(signer.getPublic().getEncoded()),
                digests.get(CertificateID.HASH_SHA1));
        builder.addResponse(id, CertificateStatus.GOOD);
        return new OCSPRespBuilder().build(OCSPRespBuilder.SUCCESSFUL, builder.build(
                new JcaContentSignerBuilder("SHA256withRSA").build(signer.getPrivate()),
                new X509CertificateHolder[0], new Date())).getEncoded();
    }

    private static byte[] query(String path) {
        return new OcspClientBouncyCastle(checked, issuer, baseUrl + path).getEncoded();
    }

    @Test
    void responseSignedByTheIssuerIsReturned() throws Exception {
        ocspBody = ocspResponse(42, issuerKeys);
        assertNotNull(query("/ocsp"));
    }

    @Test
    void responseSignedBySomebodyElseIsRejected() throws Exception {
        ocspBody = ocspResponse(42, otherKeys);
        assertThrows(RuntimeException.class, () -> query("/ocsp"));
    }

    @Test
    void responseAboutAnotherCertificateIsRejected() throws Exception {
        ocspBody = ocspResponse(43, issuerKeys);
        assertThrows(RuntimeException.class, () -> query("/ocsp"));
    }

    @Test
    void largeOcspResponseIsRejected() {
        assertThrows(RuntimeException.class, () -> query("/large"));
    }

    @Test
    void ocspUrlHasToBeHttp() {
        OcspClientBouncyCastle client = new OcspClientBouncyCastle(checked, issuer, "file:///etc/hosts");
        assertThrows(RuntimeException.class, client::getEncoded);
    }

    @Test
    void largeTsaResponseIsRejected() {
        TSAClientBouncyCastle client = new TSAClientBouncyCastle(baseUrl + "/large");
        assertThrows(IOException.class, () -> client.getTSAResponse(new byte[]{1, 2, 3}));
    }

    @Test
    void tsaUrlHasToBeHttp() {
        TSAClientBouncyCastle client = new TSAClientBouncyCastle("file:///etc/hosts");
        assertThrows(IOException.class, () -> client.getTSAResponse(new byte[]{1, 2, 3}));
    }
}
