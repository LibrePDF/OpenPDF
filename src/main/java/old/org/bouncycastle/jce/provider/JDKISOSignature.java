package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.crypto.AsymmetricBlockCipher;
import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.digests.MD5Digest;
import old.org.bouncycastle.crypto.digests.RIPEMD160Digest;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.crypto.engines.RSABlindedEngine;
import old.org.bouncycastle.crypto.signers.ISO9796d2Signer;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;

public class JDKISOSignature
    extends SignatureSpi
{
    private ISO9796d2Signer         signer;

    protected JDKISOSignature(
        Digest digest,
        AsymmetricBlockCipher cipher)
    {
        signer = new ISO9796d2Signer(cipher, digest, true);
    }

    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param = RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey);

        signer.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param = RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey);

        signer.init(true, param);
    }

    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        signer.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        signer.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        try
        {
            byte[]  sig = signer.generateSignature();

            return sig;
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
        boolean yes = signer.verifySignature(sigBytes);

        return yes;
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
        throw new UnsupportedOperationException("engineSetParameter unsupported");
    }

    static public class SHA1WithRSAEncryption
        extends JDKISOSignature
    {
        public SHA1WithRSAEncryption()
        {
            super(new SHA1Digest(), new RSABlindedEngine());
        }
    }

    static public class MD5WithRSAEncryption
        extends JDKISOSignature
    {
        public MD5WithRSAEncryption()
        {
            super(new MD5Digest(), new RSABlindedEngine());
        }
    }

    static public class RIPEMD160WithRSAEncryption
        extends JDKISOSignature
    {
        public RIPEMD160WithRSAEncryption()
        {
            super(new RIPEMD160Digest(), new RSABlindedEngine());
        }
    }
}
