package old.org.bouncycastle.operator.jcajce;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.jcajce.JcaJceHelper;
import old.org.bouncycastle.operator.OperatorCreationException;

class OperatorHelper
{
    private static final Map oids = new HashMap();
    private static final Map asymmetricWrapperAlgNames = new HashMap();
    private static final Map symmetricWrapperAlgNames = new HashMap();

    static
    {
        //
        // reverse mappings
        //
        oids.put(new DERObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
        oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3411WITHGOST3410");
        oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "GOST3411WITHECGOST3410");

        oids.put(new DERObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
        oids.put(new DERObjectIdentifier("1.2.840.113549.1.1.2"), "MD2WITHRSA");
        oids.put(new DERObjectIdentifier("1.2.840.10040.4.3"), "SHA1WITHDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384WITHECDSA");
        oids.put(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512WITHECDSA");
        oids.put(OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
        oids.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha224, "SHA224WITHDSA");
        oids.put(NISTObjectIdentifiers.dsa_with_sha256, "SHA256WITHDSA");

        asymmetricWrapperAlgNames.put(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()), "RSA/ECB/PKCS1Padding");

        symmetricWrapperAlgNames.put(PKCSObjectIdentifiers.id_alg_CMS3DESwrap, "DESEDEWrap");
        symmetricWrapperAlgNames.put(NISTObjectIdentifiers.id_aes128_wrap, "AESWrap");
        symmetricWrapperAlgNames.put(NISTObjectIdentifiers.id_aes192_wrap, "AESWrap");
        symmetricWrapperAlgNames.put(NISTObjectIdentifiers.id_aes256_wrap, "AESWrap");
        symmetricWrapperAlgNames.put(NTTObjectIdentifiers.id_camellia128_wrap, "CamilliaWrap");
        symmetricWrapperAlgNames.put(NTTObjectIdentifiers.id_camellia192_wrap, "CamilliaWrap");
        symmetricWrapperAlgNames.put(NTTObjectIdentifiers.id_camellia256_wrap, "CamilliaWrap");
        symmetricWrapperAlgNames.put(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap, "SEEDWrap");
    }

    private JcaJceHelper helper;

    OperatorHelper(JcaJceHelper helper)
    {
        this.helper = helper;
    }

    Cipher createAsymmetricWrapper(ASN1ObjectIdentifier algorithm)
        throws OperatorCreationException
    {
        try
        {
            String cipherName = (String)asymmetricWrapperAlgNames.get(algorithm);

            if (cipherName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createCipher(cipherName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createCipher(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new OperatorCreationException("cannot create cipher: " + e.getMessage(), e);
        }
    }

    Cipher createSymmetricWrapper(ASN1ObjectIdentifier algorithm)
        throws OperatorCreationException
    {
        try
        {
            String cipherName = (String)symmetricWrapperAlgNames.get(algorithm);

            if (cipherName != null)
            {
                try
                {
                    // this is reversed as the Sun policy files now allow unlimited strength RSA
                    return helper.createCipher(cipherName);
                }
                catch (NoSuchAlgorithmException e)
                {
                    // Ignore
                }
            }
            return helper.createCipher(algorithm.getId());
        }
        catch (GeneralSecurityException e)
        {
            throw new OperatorCreationException("cannot create cipher: " + e.getMessage(), e);
        }
    }

    MessageDigest createDigest(AlgorithmIdentifier digAlgId)
        throws GeneralSecurityException
    {
        MessageDigest dig;

        try
        {
            dig = helper.createDigest(getSignatureName(digAlgId));
        }
        catch (NoSuchAlgorithmException e)
        {
            //
            // try an alternate
            //
            if (oids.get(digAlgId.getAlgorithm()) != null)
            {
                String  digestAlgorithm = (String)oids.get(digAlgId.getAlgorithm());

                dig = helper.createDigest(digestAlgorithm);
            }
            else
            {
                throw e;
            }
        }

        return dig;
    }

    Signature createSignature(AlgorithmIdentifier sigAlgId)
        throws GeneralSecurityException
    {
        Signature   sig;

        try
        {
            sig = helper.createSignature(getSignatureName(sigAlgId));
        }
        catch (NoSuchAlgorithmException e)
        {
            //
            // try an alternate
            //
            if (oids.get(sigAlgId.getAlgorithm()) != null)
            {
                String  signatureAlgorithm = (String)oids.get(sigAlgId.getAlgorithm());

                sig = helper.createSignature(signatureAlgorithm);
            }
            else
            {
                throw e;
            }
        }

        return sig;
    }

    public Signature createRawSignature(AlgorithmIdentifier algorithm)
    {
        Signature   sig;

        try
        {
            String algName = getSignatureName(algorithm);
    
            algName = "NONE" + algName.substring(algName.indexOf("WITH"));

            sig = helper.createSignature(algName);
        }
        catch (Exception e)
        {
            return null;
        }

        return sig;
    }

    private static String getSignatureName(
        AlgorithmIdentifier sigAlgId)
    {
        DEREncodable params = sigAlgId.getParameters();

        if (params != null && !DERNull.INSTANCE.equals(params))
        {
            if (sigAlgId.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS))
            {
                RSASSAPSSparams rsaParams = RSASSAPSSparams.getInstance(params);
                return getDigestAlgName(rsaParams.getHashAlgorithm().getAlgorithm()) + "withRSAandMGF1";
            }
        }

        if (oids.containsKey(sigAlgId.getAlgorithm()))
        {
            return (String)oids.get(sigAlgId.getAlgorithm());
        }

        return sigAlgId.getAlgorithm().getId();
    }

    private static String getDigestAlgName(
        DERObjectIdentifier digestAlgOID)
    {
        if (PKCSObjectIdentifiers.md5.equals(digestAlgOID))
        {
            return "MD5";
        }
        else if (OIWObjectIdentifiers.idSHA1.equals(digestAlgOID))
        {
            return "SHA1";
        }
        else if (NISTObjectIdentifiers.id_sha224.equals(digestAlgOID))
        {
            return "SHA224";
        }
        else if (NISTObjectIdentifiers.id_sha256.equals(digestAlgOID))
        {
            return "SHA256";
        }
        else if (NISTObjectIdentifiers.id_sha384.equals(digestAlgOID))
        {
            return "SHA384";
        }
        else if (NISTObjectIdentifiers.id_sha512.equals(digestAlgOID))
        {
            return "SHA512";
        }
        else if (TeleTrusTObjectIdentifiers.ripemd128.equals(digestAlgOID))
        {
            return "RIPEMD128";
        }
        else if (TeleTrusTObjectIdentifiers.ripemd160.equals(digestAlgOID))
        {
            return "RIPEMD160";
        }
        else if (TeleTrusTObjectIdentifiers.ripemd256.equals(digestAlgOID))
        {
            return "RIPEMD256";
        }
        else if (CryptoProObjectIdentifiers.gostR3411.equals(digestAlgOID))
        {
            return "GOST3411";
        }
        else
        {
            return digestAlgOID.getId();
        }
    }

    public X509Certificate convertCertificate(X509CertificateHolder certHolder)
        throws CertificateException
    {

        try
        {
            CertificateFactory certFact = helper.createCertificateFactory("X.509");

            return (X509Certificate)certFact.generateCertificate(new ByteArrayInputStream(certHolder.getEncoded()));
        }
        catch (IOException e)
        {
            throw new OpCertificateException("cannot get encoded form of certificate: " + e.getMessage(), e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new OpCertificateException("cannot create certificate factory: " + e.getMessage(), e);
        }
        catch (NoSuchProviderException e)
        {
            throw new OpCertificateException("cannot find factory provider: " + e.getMessage(), e);
        }
    }

    // TODO: put somewhere public so cause easily accessed
    private static class OpCertificateException
        extends CertificateException
    {
        private Throwable cause;

        public OpCertificateException(String msg, Throwable cause)
        {
            super(msg);

            this.cause = cause;
        }

        public Throwable getCause()
        {
            return cause;
        }
    }
}
