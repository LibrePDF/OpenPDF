package old.org.bouncycastle.jce.examples;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import old.org.bouncycastle.asn1.DERBMPString;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.BasicConstraints;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.PrincipalUtil;
import old.org.bouncycastle.jce.X509Principal;
import old.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.x509.X509V1CertificateGenerator;
import old.org.bouncycastle.x509.X509V3CertificateGenerator;
import old.org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import old.org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

/**
 * Example of how to set up a certificiate chain and a PKCS 12 store for
 * a private individual - obviously you'll need to generate your own keys,
 * and you may need to add a NetscapeCertType extension or add a key
 * usage extension depending on your application, but you should get the
 * idea! As always this is just an example...
 */
public class PKCS12Example
{
    static char[]   passwd = { 'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
    
    static X509V1CertificateGenerator  v1CertGen = new X509V1CertificateGenerator();
    static X509V3CertificateGenerator  v3CertGen = new X509V3CertificateGenerator();

    /**
     * we generate the CA's certificate
     */
    public static Certificate createMasterCert(
        PublicKey       pubKey,
        PrivateKey      privKey)
        throws Exception
    {
        //
        // signers name 
        //
        String  issuer = "C=AU, O=The Legion of the Bouncy Castle, OU=Bouncy Primary Certificate";

        //
        // subjects name - the same as we are self signed.
        //
        String  subject = "C=AU, O=The Legion of the Bouncy Castle, OU=Bouncy Primary Certificate";

        //
        // create the certificate - version 1
        //

        v1CertGen.setSerialNumber(BigInteger.valueOf(1));
        v1CertGen.setIssuerDN(new X509Principal(issuer));
        v1CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        v1CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30)));
        v1CertGen.setSubjectDN(new X509Principal(subject));
        v1CertGen.setPublicKey(pubKey);
        v1CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        X509Certificate cert = v1CertGen.generate(privKey);

        cert.checkValidity(new Date());

        cert.verify(pubKey);

        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;

        //
        // this is actually optional - but if you want to have control
        // over setting the friendly name this is the way to do it...
        //
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
            new DERBMPString("Bouncy Primary Certificate"));

        return cert;
    }

    /**
     * we generate an intermediate certificate signed by our CA
     */
    public static Certificate createIntermediateCert(
        PublicKey       pubKey,
        PrivateKey      caPrivKey,
        X509Certificate caCert)
        throws Exception
    {
        //
        // subject name table.
        //
        Hashtable                   attrs = new Hashtable();
        Vector                      order = new Vector();

        attrs.put(X509Principal.C, "AU");
        attrs.put(X509Principal.O, "The Legion of the Bouncy Castle");
        attrs.put(X509Principal.OU, "Bouncy Intermediate Certificate");
        attrs.put(X509Principal.EmailAddress, "feedback-crypto@bouncycastle.org");

        order.addElement(X509Principal.C);
        order.addElement(X509Principal.O);
        order.addElement(X509Principal.OU);
        order.addElement(X509Principal.EmailAddress);

        //
        // create the certificate - version 3
        //
        v3CertGen.reset();

        v3CertGen.setSerialNumber(BigInteger.valueOf(2));
        v3CertGen.setIssuerDN(PrincipalUtil.getSubjectX509Principal(caCert));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30)));
        v3CertGen.setSubjectDN(new X509Principal(order, attrs));
        v3CertGen.setPublicKey(pubKey);
        v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        //
        // extensions
        //
        v3CertGen.addExtension(
            X509Extensions.SubjectKeyIdentifier,
            false,
            new SubjectKeyIdentifierStructure(pubKey));

        v3CertGen.addExtension(
            X509Extensions.AuthorityKeyIdentifier,
            false,
            new AuthorityKeyIdentifierStructure(caCert));

        v3CertGen.addExtension(
            X509Extensions.BasicConstraints,
            true,
            new BasicConstraints(0));

        X509Certificate cert = v3CertGen.generate(caPrivKey);

        cert.checkValidity(new Date());

        cert.verify(caCert.getPublicKey());

        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;

        //
        // this is actually optional - but if you want to have control
        // over setting the friendly name this is the way to do it...
        //
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
            new DERBMPString("Bouncy Intermediate Certificate"));

        return cert;
    }

    /**
     * we generate a certificate signed by our CA's intermediate certficate
     */
    public static Certificate createCert(
        PublicKey       pubKey,
        PrivateKey      caPrivKey,
        PublicKey       caPubKey)
        throws Exception
    {
        //
        // signers name table.
        //
        Hashtable                   sAttrs = new Hashtable();
        Vector                      sOrder = new Vector();

        sAttrs.put(X509Principal.C, "AU");
        sAttrs.put(X509Principal.O, "The Legion of the Bouncy Castle");
        sAttrs.put(X509Principal.OU, "Bouncy Intermediate Certificate");
        sAttrs.put(X509Principal.EmailAddress, "feedback-crypto@bouncycastle.org");

        sOrder.addElement(X509Principal.C);
        sOrder.addElement(X509Principal.O);
        sOrder.addElement(X509Principal.OU);
        sOrder.addElement(X509Principal.EmailAddress);

        //
        // subjects name table.
        //
        Hashtable                   attrs = new Hashtable();
        Vector                      order = new Vector();

        attrs.put(X509Principal.C, "AU");
        attrs.put(X509Principal.O, "The Legion of the Bouncy Castle");
        attrs.put(X509Principal.L, "Melbourne");
        attrs.put(X509Principal.CN, "Eric H. Echidna");
        attrs.put(X509Principal.EmailAddress, "feedback-crypto@bouncycastle.org");

        order.addElement(X509Principal.C);
        order.addElement(X509Principal.O);
        order.addElement(X509Principal.L);
        order.addElement(X509Principal.CN);
        order.addElement(X509Principal.EmailAddress);

        //
        // create the certificate - version 3
        //
        v3CertGen.reset();

        v3CertGen.setSerialNumber(BigInteger.valueOf(3));
        v3CertGen.setIssuerDN(new X509Principal(sOrder, sAttrs));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30)));
        v3CertGen.setSubjectDN(new X509Principal(order, attrs));
        v3CertGen.setPublicKey(pubKey);
        v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        //
        // add the extensions
        //
        v3CertGen.addExtension(
            X509Extensions.SubjectKeyIdentifier,
            false,
            new SubjectKeyIdentifierStructure(pubKey));

        v3CertGen.addExtension(
            X509Extensions.AuthorityKeyIdentifier,
            false,
            new AuthorityKeyIdentifierStructure(caPubKey));

        X509Certificate cert = v3CertGen.generate(caPrivKey);

        cert.checkValidity(new Date());

        cert.verify(caPubKey);

        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;

        //
        // this is also optional - in the sense that if you leave this
        // out the keystore will add it automatically, note though that
        // for the browser to recognise the associated private key this
        // you should at least use the pkcs_9_localKeyId OID and set it
        // to the same as you do for the private key's localKeyId.
        //
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
            new DERBMPString("Eric's Key"));
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
            new SubjectKeyIdentifierStructure(pubKey));

        return cert;
    }

    public static void main(
        String[]    args)
        throws Exception
    {
        Security.addProvider(new BouncyCastleProvider());

        //
        // personal keys
        //
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(
            new BigInteger("b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7", 16),
            new BigInteger("11", 16));

        RSAPrivateCrtKeySpec privKeySpec = new RSAPrivateCrtKeySpec(
            new BigInteger("b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7", 16),
            new BigInteger("11", 16),
            new BigInteger("9f66f6b05410cd503b2709e88115d55daced94d1a34d4e32bf824d0dde6028ae79c5f07b580f5dce240d7111f7ddb130a7945cd7d957d1920994da389f490c89", 16),
            new BigInteger("c0a0758cdf14256f78d4708c86becdead1b50ad4ad6c5c703e2168fbf37884cb", 16),
            new BigInteger("f01734d7960ea60070f1b06f2bb81bfac48ff192ae18451d5e56c734a5aab8a5", 16),
            new BigInteger("b54bb9edff22051d9ee60f9351a48591b6500a319429c069a3e335a1d6171391", 16),
            new BigInteger("d3d83daf2a0cecd3367ae6f8ae1aeb82e9ac2f816c6fc483533d8297dd7884cd", 16),
            new BigInteger("b8f52fc6f38593dabb661d3f50f8897f8106eee68b1bce78a95b132b4e5b5d19", 16));

        //
        // intermediate keys.
        //
        RSAPublicKeySpec intPubKeySpec = new RSAPublicKeySpec(
            new BigInteger("8de0d113c5e736969c8d2b047a243f8fe18edad64cde9e842d3669230ca486f7cfdde1f8eec54d1905fff04acc85e61093e180cadc6cea407f193d44bb0e9449b8dbb49784cd9e36260c39e06a947299978c6ed8300724e887198cfede20f3fbde658fa2bd078be946a392bd349f2b49c486e20c405588e306706c9017308e69", 16),
            new BigInteger("ffff", 16));


        RSAPrivateCrtKeySpec intPrivKeySpec = new RSAPrivateCrtKeySpec(
            new BigInteger("8de0d113c5e736969c8d2b047a243f8fe18edad64cde9e842d3669230ca486f7cfdde1f8eec54d1905fff04acc85e61093e180cadc6cea407f193d44bb0e9449b8dbb49784cd9e36260c39e06a947299978c6ed8300724e887198cfede20f3fbde658fa2bd078be946a392bd349f2b49c486e20c405588e306706c9017308e69", 16),
            new BigInteger("ffff", 16),
            new BigInteger("7deb1b194a85bcfd29cf871411468adbc987650903e3bacc8338c449ca7b32efd39ffc33bc84412fcd7df18d23ce9d7c25ea910b1ae9985373e0273b4dca7f2e0db3b7314056ac67fd277f8f89cf2fd73c34c6ca69f9ba477143d2b0e2445548aa0b4a8473095182631da46844c356f5e5c7522eb54b5a33f11d730ead9c0cff", 16),
            new BigInteger("ef4cede573cea47f83699b814de4302edb60eefe426c52e17bd7870ec7c6b7a24fe55282ebb73775f369157726fcfb988def2b40350bdca9e5b418340288f649", 16),
            new BigInteger("97c7737d1b9a0088c3c7b528539247fd2a1593e7e01cef18848755be82f4a45aa093276cb0cbf118cb41117540a78f3fc471ba5d69f0042274defc9161265721", 16),
            new BigInteger("6c641094e24d172728b8da3c2777e69adfd0839085be7e38c7c4a2dd00b1ae969f2ec9d23e7e37090fcd449a40af0ed463fe1c612d6810d6b4f58b7bfa31eb5f", 16),
            new BigInteger("70b7123e8e69dfa76feb1236d0a686144b00e9232ed52b73847e74ef3af71fb45ccb24261f40d27f98101e230cf27b977a5d5f1f15f6cf48d5cb1da2a3a3b87f", 16),
            new BigInteger("e38f5750d97e270996a286df2e653fd26c242106436f5bab0f4c7a9e654ce02665d5a281f2c412456f2d1fa26586ef04a9adac9004ca7f913162cb28e13bf40d", 16));

        //
        // ca keys
        //
        RSAPublicKeySpec caPubKeySpec = new RSAPublicKeySpec(
            new BigInteger("b259d2d6e627a768c94be36164c2d9fc79d97aab9253140e5bf17751197731d6f7540d2509e7b9ffee0a70a6e26d56e92d2edd7f85aba85600b69089f35f6bdbf3c298e05842535d9f064e6b0391cb7d306e0a2d20c4dfb4e7b49a9640bdea26c10ad69c3f05007ce2513cee44cfe01998e62b6c3637d3fc0391079b26ee36d5", 16),
            new BigInteger("11", 16));

        RSAPrivateCrtKeySpec   caPrivKeySpec = new RSAPrivateCrtKeySpec(
            new BigInteger("b259d2d6e627a768c94be36164c2d9fc79d97aab9253140e5bf17751197731d6f7540d2509e7b9ffee0a70a6e26d56e92d2edd7f85aba85600b69089f35f6bdbf3c298e05842535d9f064e6b0391cb7d306e0a2d20c4dfb4e7b49a9640bdea26c10ad69c3f05007ce2513cee44cfe01998e62b6c3637d3fc0391079b26ee36d5", 16),
            new BigInteger("11", 16),
            new BigInteger("92e08f83cc9920746989ca5034dcb384a094fb9c5a6288fcc4304424ab8f56388f72652d8fafc65a4b9020896f2cde297080f2a540e7b7ce5af0b3446e1258d1dd7f245cf54124b4c6e17da21b90a0ebd22605e6f45c9f136d7a13eaac1c0f7487de8bd6d924972408ebb58af71e76fd7b012a8d0e165f3ae2e5077a8648e619", 16),
            new BigInteger("f75e80839b9b9379f1cf1128f321639757dba514642c206bbbd99f9a4846208b3e93fbbe5e0527cc59b1d4b929d9555853004c7c8b30ee6a213c3d1bb7415d03", 16),
            new BigInteger("b892d9ebdbfc37e397256dd8a5d3123534d1f03726284743ddc6be3a709edb696fc40c7d902ed804c6eee730eee3d5b20bf6bd8d87a296813c87d3b3cc9d7947", 16),
            new BigInteger("1d1a2d3ca8e52068b3094d501c9a842fec37f54db16e9a67070a8b3f53cc03d4257ad252a1a640eadd603724d7bf3737914b544ae332eedf4f34436cac25ceb5", 16),
            new BigInteger("6c929e4e81672fef49d9c825163fec97c4b7ba7acb26c0824638ac22605d7201c94625770984f78a56e6e25904fe7db407099cad9b14588841b94f5ab498dded", 16),
            new BigInteger("dae7651ee69ad1d081ec5e7188ae126f6004ff39556bde90e0b870962fa7b926d070686d8244fe5a9aa709a95686a104614834b0ada4b10f53197a5cb4c97339", 16));



        //
        // set up the keys
        //
        KeyFactory          fact = KeyFactory.getInstance("RSA", "BC");
        PrivateKey          caPrivKey = fact.generatePrivate(caPrivKeySpec);
        PublicKey           caPubKey = fact.generatePublic(caPubKeySpec);
        PrivateKey          intPrivKey = fact.generatePrivate(intPrivKeySpec);
        PublicKey           intPubKey = fact.generatePublic(intPubKeySpec);
        PrivateKey          privKey = fact.generatePrivate(privKeySpec);
        PublicKey           pubKey = fact.generatePublic(pubKeySpec);

        Certificate[] chain = new Certificate[3];

        chain[2] = createMasterCert(caPubKey, caPrivKey);
        chain[1] = createIntermediateCert(intPubKey, caPrivKey, (X509Certificate)chain[2]);
        chain[0] = createCert(pubKey, intPrivKey, intPubKey);

        //
        // add the friendly name for the private key
        //
        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;

        //
        // this is also optional - in the sense that if you leave this
        // out the keystore will add it automatically, note though that
        // for the browser to recognise which certificate the private key
        // is associated with you should at least use the pkcs_9_localKeyId
        // OID and set it to the same as you do for the private key's
        // corresponding certificate.
        //
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
            new DERBMPString("Eric's Key"));
        bagAttr.setBagAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
            new SubjectKeyIdentifierStructure(pubKey));

        //
        // store the key and the certificate chain
        //
        KeyStore store = KeyStore.getInstance("PKCS12", "BC");

        store.load(null, null);

        //
        // if you haven't set the friendly name and local key id above
        // the name below will be the name of the key
        //
        store.setKeyEntry("Eric's Key", privKey, null, chain);

        FileOutputStream fOut = new FileOutputStream("id.p12");

        store.store(fOut, passwd);
        
        fOut.close();
    }
}
