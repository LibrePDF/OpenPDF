package old.org.bouncycastle.mail.smime.examples;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import old.org.bouncycastle.asn1.smime.SMIMECapability;
import old.org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import old.org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.cert.X509v3CertificateBuilder;
import old.org.bouncycastle.cert.jcajce.JcaCertStore;
import old.org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import old.org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import old.org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import old.org.bouncycastle.mail.smime.SMIMESignedGenerator;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import old.org.bouncycastle.util.Store;

/**
 * a simple example that creates a single signed multipart mail message.
 */
public class CreateSignedMultipartMail
{
    //
    // certificate serial number seed.
    //
    static int  serialNo = 1;

    static AuthorityKeyIdentifier createAuthorityKeyId(
        PublicKey pub) 
        throws IOException
    {
        ByteArrayInputStream bIn = new ByteArrayInputStream(pub.getEncoded());
        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
            (ASN1Sequence)new ASN1InputStream(bIn).readObject());

        return new AuthorityKeyIdentifier(info);
    }

    static SubjectKeyIdentifier createSubjectKeyId(
        PublicKey pub) 
        throws IOException
    {
        ByteArrayInputStream bIn = new ByteArrayInputStream(pub.getEncoded());

        SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
            (ASN1Sequence)new ASN1InputStream(bIn).readObject());

        return new SubjectKeyIdentifier(info);
    }

    /**
     * create a basic X509 certificate from the given keys
     */
    static X509Certificate makeCertificate(
        KeyPair subKP,
        String  subDN,
        KeyPair issKP,
        String  issDN)
        throws GeneralSecurityException, IOException, OperatorCreationException
    {
        PublicKey  subPub  = subKP.getPublic();
        PrivateKey issPriv = issKP.getPrivate();
        PublicKey  issPub  = issKP.getPublic();

        X509v3CertificateBuilder v3CertGen = new JcaX509v3CertificateBuilder(new X500Name(issDN), BigInteger.valueOf(serialNo++), new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 100)), new X500Name(subDN), subPub);

        v3CertGen.addExtension(
            X509Extension.subjectKeyIdentifier,
            false,
            createSubjectKeyId(subPub));

        v3CertGen.addExtension(
            X509Extension.authorityKeyIdentifier,
            false,
            createAuthorityKeyId(issPub));

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(v3CertGen.build(new JcaContentSignerBuilder("MD5withRSA").setProvider("BC").build(issPriv)));
    }

    public static void main(
        String args[])
        throws Exception
    {
        //
        // set up our certs
        //
        KeyPairGenerator    kpg  = KeyPairGenerator.getInstance("RSA", "BC");

        kpg.initialize(1024, new SecureRandom());

        //
        // cert that issued the signing certificate
        //
        String              signDN = "O=Bouncy Castle, C=AU";
        KeyPair             signKP = kpg.generateKeyPair();
        X509Certificate     signCert = makeCertificate(
                                        signKP, signDN, signKP, signDN);

        //
        // cert we sign against
        //
        String              origDN = "CN=Eric H. Echidna, E=eric@bouncycastle.org, O=Bouncy Castle, C=AU";
        KeyPair             origKP = kpg.generateKeyPair();
        X509Certificate     origCert = makeCertificate(
                                        origKP, origDN, signKP, signDN);

        List                certList = new ArrayList();

        certList.add(origCert);
        certList.add(signCert);

        //
        // create a CertStore containing the certificates we want carried
        // in the signature
        //
        Store certs = new JcaCertStore(certList);

        //
        // create some smime capabilities in case someone wants to respond
        //
        ASN1EncodableVector         signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector       caps = new SMIMECapabilityVector();

        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.rC2_CBC, 128);
        caps.addCapability(SMIMECapability.dES_CBC);

        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

        //
        // add an encryption key preference for encrypted responses -
        // normally this would be different from the signing certificate...
        //
        IssuerAndSerialNumber   issAndSer = new IssuerAndSerialNumber(
                new X500Name(signDN), origCert.getSerialNumber());

        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));

        //
        // create the generator for creating an smime/signed message
        //
        SMIMESignedGenerator gen = new SMIMESignedGenerator();

        //
        // add a signer to the generator - this specifies we are using SHA1 and
        // adding the smime attributes above to the signed attributes that
        // will be generated as part of the signature. The encryption algorithm
        // used is taken from the key - in this RSA with PKCS1Padding
        //
        gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build("SHA1withRSA", origKP.getPrivate(), origCert));

        //
        // add our pool of certs and cerls (if any) to go with the signature
        //
        gen.addCertificates(certs);

        //
        // create the base for our message
        //
        MimeBodyPart    msg1 = new MimeBodyPart();

        msg1.setText("Hello part 1!");

        MimeBodyPart    msg2 = new MimeBodyPart();

        msg2.setText("Hello part 2!");

        MimeMultipart mp = new MimeMultipart();

        mp.addBodyPart(msg1);
        mp.addBodyPart(msg2);

        MimeBodyPart m = new MimeBodyPart();

        //
        // be careful about setting extra headers here. Some mail clients
        // ignore the To and From fields (for example) in the body part
        // that contains the multipart. The result of this will be that the
        // signature fails to verify... Outlook Express is an example of
        // a client that exhibits this behaviour.
        //
        m.setContent(mp);

        //
        // extract the multipart object from the SMIMESigned object.
        //
        MimeMultipart mm = gen.generate(m);

        //
        // Get a Session object and create the mail message
        //
        Properties props = System.getProperties();
        Session session = Session.getDefaultInstance(props, null);

        Address fromUser = new InternetAddress("\"Eric H. Echidna\"<eric@bouncycastle.org>");
        Address toUser = new InternetAddress("example@bouncycastle.org");

        MimeMessage body = new MimeMessage(session);
        body.setFrom(fromUser);
        body.setRecipient(Message.RecipientType.TO, toUser);
        body.setSubject("example signed message");
        body.setContent(mm, mm.getContentType());
        body.saveChanges();

        body.writeTo(new FileOutputStream("signed.message"));
    }
}
