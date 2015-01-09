package old.org.bouncycastle.jce.provider;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.DigestInfo;
import old.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import old.org.bouncycastle.crypto.AsymmetricBlockCipher;
import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.digests.MD2Digest;
import old.org.bouncycastle.crypto.digests.MD4Digest;
import old.org.bouncycastle.crypto.digests.MD5Digest;
import old.org.bouncycastle.crypto.digests.NullDigest;
import old.org.bouncycastle.crypto.digests.RIPEMD128Digest;
import old.org.bouncycastle.crypto.digests.RIPEMD160Digest;
import old.org.bouncycastle.crypto.digests.RIPEMD256Digest;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.crypto.digests.SHA224Digest;
import old.org.bouncycastle.crypto.digests.SHA256Digest;
import old.org.bouncycastle.crypto.digests.SHA384Digest;
import old.org.bouncycastle.crypto.digests.SHA512Digest;
import old.org.bouncycastle.crypto.encodings.PKCS1Encoding;
import old.org.bouncycastle.crypto.engines.RSABlindedEngine;

public class JDKDigestSignature
    extends SignatureSpi
{
    private Digest                  digest;
    private AsymmetricBlockCipher   cipher;
    private AlgorithmIdentifier     algId;

    // care - this constructor is actually used by outside organisations
    protected JDKDigestSignature(
        Digest                  digest,
        AsymmetricBlockCipher   cipher)
    {
        this.digest = digest;
        this.cipher = cipher;
        this.algId = null;
    }

    // care - this constructor is actually used by outside organisations
    protected JDKDigestSignature(
        DERObjectIdentifier     objId,
        Digest                  digest,
        AsymmetricBlockCipher   cipher)
    {
        this.digest = digest;
        this.cipher = cipher;
        this.algId = new AlgorithmIdentifier(objId, DERNull.INSTANCE);
    }

    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        if (!(publicKey instanceof RSAPublicKey))
        {
            throw new InvalidKeyException("Supplied key (" + getType(publicKey) + ") is not a RSAPublicKey instance");
        }

        CipherParameters    param = RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey);

        digest.reset();
        cipher.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        if (!(privateKey instanceof RSAPrivateKey))
        {
            throw new InvalidKeyException("Supplied key (" + getType(privateKey) + ") is not a RSAPrivateKey instance");
        }

        CipherParameters    param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey);

        digest.reset();

        cipher.init(true, param);
    }

    private String getType(
        Object o)
    {
        if (o == null)
        {
            return null;
        }
        
        return o.getClass().getName();
    }
    
    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        digest.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        digest.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        try
        {
            byte[]  bytes = derEncode(hash);

            return cipher.processBlock(bytes, 0, bytes.length);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new SignatureException("key too small for signature type");
        }
        catch (Exception e)
        {
            throw new SignatureException(e.toString());
        }
    }

    protected boolean engineVerify(
        byte[]  sigBytes) 
        throws SignatureException
    {
        byte[]  hash = new byte[digest.getDigestSize()];

        digest.doFinal(hash, 0);

        byte[]      sig;
        byte[]      expected;

        try
        {
            sig = cipher.processBlock(sigBytes, 0, sigBytes.length);

            expected = derEncode(hash);
        }
        catch (Exception e)
        {
            return false;
        }

        if (sig.length == expected.length)
        {
            for (int i = 0; i < sig.length; i++)
            {
                if (sig[i] != expected[i])
                {
                    return false;
                }
            }
        }
        else if (sig.length == expected.length - 2)  // NULL left out
        {
            int sigOffset = sig.length - hash.length - 2;
            int expectedOffset = expected.length - hash.length - 2;

            expected[1] -= 2;      // adjust lengths
            expected[3] -= 2;

            for (int i = 0; i < hash.length; i++)
            {
                if (sig[sigOffset + i] != expected[expectedOffset + i])  // check hash
                {
                    return false;
                }
            }

            for (int i = 0; i < sigOffset; i++)
            {
                if (sig[i] != expected[i])  // check header less NULL
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    protected void engineSetParameter(
        AlgorithmParameterSpec params)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated replaced with <a href = "#engineSetParameter(java.security.spec.AlgorithmParameterSpec)">
     */
    protected void engineSetParameter(
        String  param,
        Object  value)
    {
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    /**
     * @deprecated
     */
    protected Object engineGetParameter(
        String      param)
    {
        return null;
    }

    protected AlgorithmParameters engineGetParameters()
    {
        return null;
    }

    private byte[] derEncode(
        byte[]  hash)
        throws IOException
    {
        if (algId == null)
        {
            // For raw RSA, the DigestInfo must be prepared externally
            return hash;
        }

        DigestInfo              dInfo = new DigestInfo(algId, hash);

        return dInfo.getEncoded(ASN1Encodable.DER);
    }

    static public class SHA1WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA1WithRSAEncryption()
        {
            super(X509ObjectIdentifiers.id_SHA1, new SHA1Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class SHA224WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA224WithRSAEncryption()
        {
            super(NISTObjectIdentifiers.id_sha224, new SHA224Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class SHA256WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA256WithRSAEncryption()
        {
            super(NISTObjectIdentifiers.id_sha256, new SHA256Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class SHA384WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA384WithRSAEncryption()
        {
            super(NISTObjectIdentifiers.id_sha384, new SHA384Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class SHA512WithRSAEncryption
        extends JDKDigestSignature
    {
        public SHA512WithRSAEncryption()
        {
            super(NISTObjectIdentifiers.id_sha512, new SHA512Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class MD2WithRSAEncryption
        extends JDKDigestSignature
    {
        public MD2WithRSAEncryption()
        {
            super(PKCSObjectIdentifiers.md2, new MD2Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class MD4WithRSAEncryption
        extends JDKDigestSignature
    {
        public MD4WithRSAEncryption()
        {
            super(PKCSObjectIdentifiers.md4, new MD4Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class MD5WithRSAEncryption
        extends JDKDigestSignature
    {
        public MD5WithRSAEncryption()
        {
            super(PKCSObjectIdentifiers.md5, new MD5Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class RIPEMD160WithRSAEncryption
        extends JDKDigestSignature
    {
        public RIPEMD160WithRSAEncryption()
        {
            super(TeleTrusTObjectIdentifiers.ripemd160, new RIPEMD160Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class RIPEMD128WithRSAEncryption
        extends JDKDigestSignature
    {
        public RIPEMD128WithRSAEncryption()
        {
            super(TeleTrusTObjectIdentifiers.ripemd128, new RIPEMD128Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class RIPEMD256WithRSAEncryption
        extends JDKDigestSignature
    {
        public RIPEMD256WithRSAEncryption()
        {
            super(TeleTrusTObjectIdentifiers.ripemd256, new RIPEMD256Digest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }

    static public class noneRSA
        extends JDKDigestSignature
    {
        public noneRSA()
        {
            super(new NullDigest(), new PKCS1Encoding(new RSABlindedEngine()));
        }
    }
}
