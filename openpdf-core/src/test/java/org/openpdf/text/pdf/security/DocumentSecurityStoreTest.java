package org.openpdf.text.pdf.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DocumentSecurityStoreTest {

    @BeforeAll
    static void installBc() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Test
    void buildEmptyDssHasNoArrays() throws Exception {
        PdfWriter writer = createWriter();
        PdfDictionary dss = new DocumentSecurityStore().build(writer);
        assertNotNull(dss);
        // empty store should produce a Type=DSS dict with no Certs/CRLs/OCSPs entries
        assertNull(dss.get(PdfName.CERTS), "Empty DSS should have no /Certs");
        assertNull(dss.get(PdfName.CRLS), "Empty DSS should have no /CRLs");
        assertNull(dss.get(PdfName.OCSPS), "Empty DSS should have no /OCSPs");
    }

    @Test
    void buildDssWithCertHasCertsArray() throws Exception {
        X509Certificate cert = selfSignedCert();
        DocumentSecurityStore dss = new DocumentSecurityStore();
        dss.addCertificate(cert);

        PdfWriter writer = createWriter();
        PdfDictionary result = dss.build(writer);

        PdfArray certs = result.getAsArray(PdfName.CERTS);
        assertNotNull(certs, "DSS with a certificate must have /Certs");
        assertTrue(certs.size() > 0);
    }

    @Test
    void buildDssWithOcspHasOcspsArray() throws Exception {
        DocumentSecurityStore dss = new DocumentSecurityStore();
        dss.addOcsp(new byte[]{0x30, 0x00}); // minimal placeholder DER

        PdfWriter writer = createWriter();
        PdfDictionary result = dss.build(writer);

        PdfArray ocsps = result.getAsArray(PdfName.OCSPS);
        assertNotNull(ocsps, "DSS with an OCSP response must have /OCSPs");
        assertTrue(ocsps.size() > 0);
    }

    @Test
    void buildDssWithCrlBytesHasCrlsArray() throws Exception {
        DocumentSecurityStore dss = new DocumentSecurityStore();
        dss.addCrl(new byte[]{0x30, 0x00}); // minimal placeholder DER

        PdfWriter writer = createWriter();
        PdfDictionary result = dss.build(writer);

        PdfArray crls = result.getAsArray(PdfName.CRLS);
        assertNotNull(crls, "DSS with a CRL must have /CRLs");
        assertTrue(crls.size() > 0);
    }

    @Test
    void addNullCertIsIgnored() throws Exception {
        DocumentSecurityStore dss = new DocumentSecurityStore();
        dss.addCertificate(null); // must not throw

        PdfWriter writer = createWriter();
        PdfDictionary result = dss.build(writer);
        assertNull(result.get(PdfName.CERTS));
    }

    // ---- helpers ----

    private static PdfWriter createWriter() throws Exception {
        Document doc = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return PdfWriter.getInstance(doc, baos);
    }

    private static X509Certificate selfSignedCert() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(1024);
        KeyPair kp = gen.generateKeyPair();

        X500Name subject = new X500Name("CN=DSS Test");
        BigInteger serial = BigInteger.ONE;
        Date now = new Date();
        Date later = new Date(now.getTime() + 86400_000L);

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC").build(kp.getPrivate());

        return new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(new JcaX509v3CertificateBuilder(
                        subject, serial, now, later, subject, kp.getPublic()).build(signer));
    }
}
