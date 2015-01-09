package old.org.bouncycastle.jce;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.CertificationRequest;
import old.org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509Name;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.util.Strings;

/**
 * A class for verifying and creating PKCS10 Certification requests. 
 * <pre>
 * CertificationRequest ::= SEQUENCE {
 *   certificationRequestInfo  CertificationRequestInfo,
 *   signatureAlgorithm        AlgorithmIdentifier{{ SignatureAlgorithms }},
 *   signature                 BIT STRING
 * }
 *
 * CertificationRequestInfo ::= SEQUENCE {
 *   version             INTEGER { v1(0) } (v1,...),
 *   subject             Name,
 *   subjectPKInfo   SubjectPublicKeyInfo{{ PKInfoAlgorithms }},
 *   attributes          [0] Attributes{{ CRIAttributes }}
 *  }
 *
 *  Attributes { ATTRIBUTE:IOSet } ::= SET OF Attribute{{ IOSet }}
 *
 *  Attribute { ATTRIBUTE:IOSet } ::= SEQUENCE {
 *    type    ATTRIBUTE.&id({IOSet}),
 *    values  SET SIZE(1..MAX) OF ATTRIBUTE.&Type({IOSet}{\@type})
 *  }
 * </pre>
 */
public class PKCS10CertificationRequest
    extends CertificationRequest
{
    private static Hashtable            algorithms = new Hashtable();
    private static Hashtable            params = new Hashtable();
    private static Hashtable            keyAlgorithms = new Hashtable();
    private static Hashtable            oids = new Hashtable();
    private static Set                  noParams = new HashSet();

    static
    {
        algorithms.put("MD2WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.2"));
        algorithms.put("MD2WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.2"));
        algorithms.put("MD5WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("MD5WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("RSAWITHMD5", new DERObjectIdentifier("1.2.840.113549.1.1.4"));
        algorithms.put("SHA1WITHRSAENCRYPTION", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA1WITHRSA", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("SHA224WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA224WITHRSA", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA256WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA256WITHRSA", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA384WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA384WITHRSA", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA512WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA512WITHRSA", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA1WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA224WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA256WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA384WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA512WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("RSAWITHSHA1", new DERObjectIdentifier("1.2.840.113549.1.1.5"));
        algorithms.put("RIPEMD128WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD128WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD160WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD160WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD256WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("RIPEMD256WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("SHA1WITHDSA", new DERObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("DSAWITHSHA1", new DERObjectIdentifier("1.2.840.10040.4.3"));
        algorithms.put("SHA224WITHDSA", NISTObjectIdentifiers.dsa_with_sha224);
        algorithms.put("SHA256WITHDSA", NISTObjectIdentifiers.dsa_with_sha256);
        algorithms.put("SHA384WITHDSA", NISTObjectIdentifiers.dsa_with_sha384);
        algorithms.put("SHA512WITHDSA", NISTObjectIdentifiers.dsa_with_sha512);
        algorithms.put("SHA1WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("SHA224WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA224);
        algorithms.put("SHA256WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA256);
        algorithms.put("SHA384WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA384);
        algorithms.put("SHA512WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA512);
        algorithms.put("ECDSAWITHSHA1", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("GOST3411WITHGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3410WITHGOST3411", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHECGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHECGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);

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
        
        //
        // key types
        //
        keyAlgorithms.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        keyAlgorithms.put(X9ObjectIdentifiers.id_dsa, "DSA");
        
        //
        // According to RFC 3279, the ASN.1 encoding SHALL (id-dsa-with-sha1) or MUST (ecdsa-with-SHA*) omit the parameters field. 
        // The parameters field SHALL be NULL for RSA based signature algorithms.
        //
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA1);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA224);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA256);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA384);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA512);
        noParams.add(X9ObjectIdentifiers.id_dsa_with_sha1);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha224);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha256);

        //
        // RFC 4491
        //
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        //
        // explicit params
        //
        AlgorithmIdentifier sha1AlgId = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, new DERNull());
        params.put("SHA1WITHRSAANDMGF1", creatPSSParams(sha1AlgId, 20));

        AlgorithmIdentifier sha224AlgId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha224, new DERNull());
        params.put("SHA224WITHRSAANDMGF1", creatPSSParams(sha224AlgId, 28));

        AlgorithmIdentifier sha256AlgId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, new DERNull());
        params.put("SHA256WITHRSAANDMGF1", creatPSSParams(sha256AlgId, 32));

        AlgorithmIdentifier sha384AlgId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384, new DERNull());
        params.put("SHA384WITHRSAANDMGF1", creatPSSParams(sha384AlgId, 48));

        AlgorithmIdentifier sha512AlgId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512, new DERNull());
        params.put("SHA512WITHRSAANDMGF1", creatPSSParams(sha512AlgId, 64));
    }

    private static RSASSAPSSparams creatPSSParams(AlgorithmIdentifier hashAlgId, int saltSize)
    {
        return new RSASSAPSSparams(
            hashAlgId,
            new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hashAlgId),
            new DERInteger(saltSize),
            new DERInteger(1));
    }

    private static ASN1Sequence toDERSequence(
        byte[]  bytes)
    {
        try
        {
            ASN1InputStream         dIn = new ASN1InputStream(bytes);

            return (ASN1Sequence)dIn.readObject();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("badly encoded request");
        }
    }

    /**
     * construct a PKCS10 certification request from a DER encoded
     * byte stream.
     */
    public PKCS10CertificationRequest(
        byte[]  bytes)
    {
        super(toDERSequence(bytes));
    }

    public PKCS10CertificationRequest(
        ASN1Sequence  sequence)
    {
        super(sequence);
    }

    /**
     * create a PKCS10 certfication request using the BC provider.
     */
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X509Name            subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        this(signatureAlgorithm, subject, key, attributes, signingKey, BouncyCastleProvider.PROVIDER_NAME);
    }

    private static X509Name convertName(
        X500Principal    name)
    {
        try
        {
            return new X509Principal(name.getEncoded());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't convert name");
        }
    }
    
    /**
     * create a PKCS10 certfication request using the BC provider.
     */
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X500Principal       subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        this(signatureAlgorithm, convertName(subject), key, attributes, signingKey, BouncyCastleProvider.PROVIDER_NAME);
    }
    
    /**
     * create a PKCS10 certfication request using the named provider.
     */
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X500Principal       subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        this(signatureAlgorithm, convertName(subject), key, attributes, signingKey, provider);
    }
    
    /**
     * create a PKCS10 certfication request using the named provider.
     */
    public PKCS10CertificationRequest(
        String              signatureAlgorithm,
        X509Name            subject,
        PublicKey           key,
        ASN1Set             attributes,
        PrivateKey          signingKey,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        String algorithmName = Strings.toUpperCase(signatureAlgorithm);
        DERObjectIdentifier sigOID = (DERObjectIdentifier)algorithms.get(algorithmName);

        if (sigOID == null)
        {
            try
            {
                sigOID = new DERObjectIdentifier(algorithmName);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Unknown signature type requested");
            }
        }

        if (subject == null)
        {
            throw new IllegalArgumentException("subject must not be null");
        }

        if (key == null)
        {
            throw new IllegalArgumentException("public key must not be null");
        }

        if (noParams.contains(sigOID))
        {
            this.sigAlgId = new AlgorithmIdentifier(sigOID);
        }
        else if (params.containsKey(algorithmName))
        {
            this.sigAlgId = new AlgorithmIdentifier(sigOID, (DEREncodable)params.get(algorithmName));
        }
        else
        {
            this.sigAlgId = new AlgorithmIdentifier(sigOID, DERNull.INSTANCE);
        }

        try
        {
            ASN1Sequence seq = (ASN1Sequence)ASN1Object.fromByteArray(key.getEncoded());
            this.reqInfo = new CertificationRequestInfo(subject, new SubjectPublicKeyInfo(seq), attributes);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't encode public key");
        }

        Signature sig;
        if (provider == null)
        {
            sig = Signature.getInstance(signatureAlgorithm);
        }
        else
        {
            sig = Signature.getInstance(signatureAlgorithm, provider);
        }

        sig.initSign(signingKey);

        try
        {
            sig.update(reqInfo.getEncoded(ASN1Encodable.DER));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("exception encoding TBS cert request - " + e);
        }

        this.sigBits = new DERBitString(sig.sign());
    }

    /**
     * return the public key associated with the certification request -
     * the public key is created using the BC provider.
     */
    public PublicKey getPublicKey()
        throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException
    {
        return getPublicKey(BouncyCastleProvider.PROVIDER_NAME);
    }

    public PublicKey getPublicKey(
        String  provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException
    {
        SubjectPublicKeyInfo    subjectPKInfo = reqInfo.getSubjectPublicKeyInfo();
        X509EncodedKeySpec      xspec = new X509EncodedKeySpec(new DERBitString(subjectPKInfo).getBytes());
        AlgorithmIdentifier     keyAlg = subjectPKInfo.getAlgorithmId();
        
        try
        {
            try
            {
                if (provider == null)
                {
                    return KeyFactory.getInstance(keyAlg.getObjectId().getId()).generatePublic(xspec);
                }
                else
                {
                    return KeyFactory.getInstance(keyAlg.getObjectId().getId(), provider).generatePublic(xspec);
                }
            }
            catch (NoSuchAlgorithmException e)
            {
                //
                // try an alternate
                //
                if (keyAlgorithms.get(keyAlg.getObjectId()) != null)
                {
                    String  keyAlgorithm = (String)keyAlgorithms.get(keyAlg.getObjectId());
                    
                    if (provider == null)
                    {
                        return KeyFactory.getInstance(keyAlgorithm).generatePublic(xspec);
                    }
                    else
                    {
                        return KeyFactory.getInstance(keyAlgorithm, provider).generatePublic(xspec);
                    }
                }
                
                throw e;
            }
        }
        catch (InvalidKeySpecException e)
        {
            throw new InvalidKeyException("error decoding public key");
        }
    }

    /**
     * verify the request using the BC provider.
     */
    public boolean verify()
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        return verify(BouncyCastleProvider.PROVIDER_NAME);
    }

    /**
     * verify the request using the passed in provider.
     */
    public boolean verify(
        String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        return verify(this.getPublicKey(provider), provider);
    }

    /**
     * verify the request using the passed in public key and the provider..
     */
    public boolean verify(
        PublicKey pubKey,
        String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException,
                InvalidKeyException, SignatureException
    {
        Signature   sig;

        try
        {
            if (provider == null)
            {
                sig = Signature.getInstance(getSignatureName(sigAlgId));
            }
            else
            {
                sig = Signature.getInstance(getSignatureName(sigAlgId), provider);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            //
            // try an alternate
            //
            if (oids.get(sigAlgId.getObjectId()) != null)
            {
                String  signatureAlgorithm = (String)oids.get(sigAlgId.getObjectId());

                if (provider == null)
                {
                    sig = Signature.getInstance(signatureAlgorithm);
                }
                else
                {
                    sig = Signature.getInstance(signatureAlgorithm, provider);
                }
            }
            else
            {
                throw e;
            }
        }

        setSignatureParameters(sig, sigAlgId.getParameters());
        
        sig.initVerify(pubKey);

        try
        {
            sig.update(reqInfo.getEncoded(ASN1Encodable.DER));
        }
        catch (Exception e)
        {
            throw new SignatureException("exception encoding TBS cert request - " + e);
        }

        return sig.verify(sigBits.getBytes());
    }

    /**
     * return a DER encoded byte array representing this object
     */
    public byte[] getEncoded()
    {
        try
        {
            return this.getEncoded(ASN1Encodable.DER);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    private void setSignatureParameters(
        Signature signature,
        DEREncodable params)
        throws NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        if (params != null && !DERNull.INSTANCE.equals(params))
        {
            AlgorithmParameters sigParams = AlgorithmParameters.getInstance(signature.getAlgorithm(), signature.getProvider());

            try
            {
                sigParams.init(params.getDERObject().getDEREncoded());
            }
            catch (IOException e)
            {
                throw new SignatureException("IOException decoding parameters: " + e.getMessage());
            }

            if (signature.getAlgorithm().endsWith("MGF1"))
            {
                try
                {
                    signature.setParameter(sigParams.getParameterSpec(PSSParameterSpec.class));
                }
                catch (GeneralSecurityException e)
                {
                    throw new SignatureException("Exception extracting parameters: " + e.getMessage());
                }
            }
        }
    }

    static String getSignatureName(
        AlgorithmIdentifier sigAlgId)
    {
        DEREncodable params = sigAlgId.getParameters();

        if (params != null && !DERNull.INSTANCE.equals(params))
        {
            if (sigAlgId.getObjectId().equals(PKCSObjectIdentifiers.id_RSASSA_PSS))
            {
                RSASSAPSSparams rsaParams = RSASSAPSSparams.getInstance(params);
                return getDigestAlgName(rsaParams.getHashAlgorithm().getObjectId()) + "withRSAandMGF1";
            }
        }

        return sigAlgId.getObjectId().getId();
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
}
