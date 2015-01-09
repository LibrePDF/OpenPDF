package old.org.bouncycastle.jce.provider;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;

import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.DSA;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.digests.GOST3411Digest;
import old.org.bouncycastle.crypto.params.ParametersWithRandom;
import old.org.bouncycastle.crypto.signers.ECGOST3410Signer;
import old.org.bouncycastle.crypto.signers.GOST3410Signer;
import old.org.bouncycastle.jce.interfaces.ECKey;
import old.org.bouncycastle.jce.interfaces.ECPublicKey;
import old.org.bouncycastle.jce.interfaces.GOST3410Key;
import old.org.bouncycastle.jce.provider.asymmetric.ec.ECUtil;

public class JDKGOST3410Signer
    extends SignatureSpi
    implements PKCSObjectIdentifiers, X509ObjectIdentifiers
{
    private Digest                  digest;
    private DSA                     signer;
    private SecureRandom            random;

    protected JDKGOST3410Signer(
        Digest digest,
        DSA signer)
    {
        this.digest = digest;
        this.signer = signer;
    }

    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        CipherParameters    param;

        if (publicKey instanceof ECPublicKey)
        {
            param = ECUtil.generatePublicKeyParameter(publicKey);
        }
        else if (publicKey instanceof GOST3410Key)
        {
            param = GOST3410Util.generatePublicKeyParameter(publicKey);
        }
        else
        {
            try
            {
                byte[]  bytes = publicKey.getEncoded();

                publicKey = JDKKeyFactory.createPublicKeyFromDERStream(bytes);

                if (publicKey instanceof ECPublicKey)
                {
                    param = ECUtil.generatePublicKeyParameter(publicKey);
                }
                else
                {
                    throw new InvalidKeyException("can't recognise key type in DSA based signer");
                }
            }
            catch (Exception e)
            {
                throw new InvalidKeyException("can't recognise key type in DSA based signer");
            }
        }

        digest.reset();
        signer.init(false, param);
    }

    protected void engineInitSign(
        PrivateKey      privateKey,
        SecureRandom    random)
        throws InvalidKeyException
    {
        this.random = random;
        engineInitSign(privateKey);
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        CipherParameters    param;

        if (privateKey instanceof ECKey)
        {
            param = ECUtil.generatePrivateKeyParameter(privateKey);
        }
        else
        {
            param = GOST3410Util.generatePrivateKeyParameter(privateKey);
        }

        digest.reset();

        if (random != null)
        {
            signer.init(true, new ParametersWithRandom(param, random));
        }
        else
        {
            signer.init(true, param);
        }
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
            byte[]          sigBytes = new byte[64];
            BigInteger[]    sig = signer.generateSignature(hash);
            byte[]          r = sig[0].toByteArray();
            byte[]          s = sig[1].toByteArray();

            if (s[0] != 0)
            {
                System.arraycopy(s, 0, sigBytes, 32 - s.length, s.length);
            }
            else
            {
                System.arraycopy(s, 1, sigBytes, 32 - (s.length - 1), s.length - 1);
            }
            
            if (r[0] != 0)
            {
                System.arraycopy(r, 0, sigBytes, 64 - r.length, r.length);
            }
            else
            {
                System.arraycopy(r, 1, sigBytes, 64 - (r.length - 1), r.length - 1);
            }

            return sigBytes;
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

        BigInteger[]    sig;

        try
        {
            byte[] r = new byte[32]; 
            byte[] s = new byte[32];

            System.arraycopy(sigBytes, 0, s, 0, 32);

            System.arraycopy(sigBytes, 32, r, 0, 32);
            
            sig = new BigInteger[2];
            sig[0] = new BigInteger(1, r);
            sig[1] = new BigInteger(1, s);
        }
        catch (Exception e)
        {
            throw new SignatureException("error decoding signature bytes.");
        }

        return signer.verifySignature(hash, sig[0], sig[1]);
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

    static public class gost3410
        extends JDKGOST3410Signer
    {
        public gost3410()
        {
            super(new GOST3411Digest(), new GOST3410Signer());
        }
    }
    
    static public class ecgost3410
        extends JDKGOST3410Signer
    {
        public ecgost3410()
        {
            super(new GOST3411Digest(), new ECGOST3410Signer());
        }
    }
}
