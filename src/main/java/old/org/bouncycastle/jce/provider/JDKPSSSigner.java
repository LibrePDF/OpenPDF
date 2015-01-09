package old.org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.crypto.AsymmetricBlockCipher;
import old.org.bouncycastle.crypto.CryptoException;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.engines.RSABlindedEngine;
import old.org.bouncycastle.crypto.params.ParametersWithRandom;
import old.org.bouncycastle.crypto.signers.PSSSigner;

public class JDKPSSSigner
    extends SignatureSpi
{
    private AlgorithmParameters    engineParams;
    private PSSParameterSpec       paramSpec;
    private PSSParameterSpec       originalSpec;
    private AsymmetricBlockCipher  signer;
    private Digest contentDigest;
    private Digest mgfDigest;
    private int saltLength;
    private byte trailer;
    private boolean isRaw;

    private PSSSigner pss;

    private byte getTrailer(
        int trailerField)
    {
        if (trailerField == 1)
        {
            return PSSSigner.TRAILER_IMPLICIT;
        }
        
        throw new IllegalArgumentException("unknown trailer field");
    }

    private void setupContentDigest()
    {
        if (isRaw)
        {
            this.contentDigest = new NullPssDigest(mgfDigest);
        }
        else
        {
            this.contentDigest = mgfDigest;
        }
    }

    // care - this constructor is actually used by outside organisations
    protected JDKPSSSigner(
        AsymmetricBlockCipher signer,
        PSSParameterSpec paramSpecArg)
    {
        this(signer, paramSpecArg, false);
    }

    // care - this constructor is actually used by outside organisations
    protected JDKPSSSigner(
        AsymmetricBlockCipher signer,
        PSSParameterSpec baseParamSpec,
        boolean          isRaw)
    {
        this.signer = signer;
        this.originalSpec = baseParamSpec;
        
        if (baseParamSpec == null)
        {
            this.paramSpec = PSSParameterSpec.DEFAULT;
        }
        else
        {
            this.paramSpec = baseParamSpec;
        }

        this.mgfDigest = JCEDigestUtil.getDigest(paramSpec.getDigestAlgorithm());
        this.saltLength = paramSpec.getSaltLength();
        this.trailer = getTrailer(paramSpec.getTrailerField());
        this.isRaw = isRaw;

        setupContentDigest();
    }
    
    protected void engineInitVerify(
        PublicKey   publicKey)
        throws InvalidKeyException
    {
        if (!(publicKey instanceof RSAPublicKey))
        {
            throw new InvalidKeyException("Supplied key is not a RSAPublicKey instance");
        }

        pss = new PSSSigner(signer, contentDigest, mgfDigest, saltLength, trailer);
        pss.init(false,
            RSAUtil.generatePublicKeyParameter((RSAPublicKey)publicKey));
    }

    protected void engineInitSign(
        PrivateKey      privateKey,
        SecureRandom    random)
        throws InvalidKeyException
    {
        if (!(privateKey instanceof RSAPrivateKey))
        {
            throw new InvalidKeyException("Supplied key is not a RSAPrivateKey instance");
        }

        pss = new PSSSigner(signer, contentDigest, mgfDigest, saltLength, trailer);
        pss.init(true, new ParametersWithRandom(RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey), random));
    }

    protected void engineInitSign(
        PrivateKey  privateKey)
        throws InvalidKeyException
    {
        if (!(privateKey instanceof RSAPrivateKey))
        {
            throw new InvalidKeyException("Supplied key is not a RSAPrivateKey instance");
        }

        pss = new PSSSigner(signer, contentDigest, mgfDigest, saltLength, trailer);
        pss.init(true, RSAUtil.generatePrivateKeyParameter((RSAPrivateKey)privateKey));
    }

    protected void engineUpdate(
        byte    b)
        throws SignatureException
    {
        pss.update(b);
    }

    protected void engineUpdate(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        pss.update(b, off, len);
    }

    protected byte[] engineSign()
        throws SignatureException
    {
        try
        {
            return pss.generateSignature();
        }
        catch (CryptoException e)
        {
            throw new SignatureException(e.getMessage());
        }
    }

    protected boolean engineVerify(
        byte[]  sigBytes) 
        throws SignatureException
    {
        return pss.verifySignature(sigBytes);
    }

    protected void engineSetParameter(
        AlgorithmParameterSpec params)
        throws InvalidParameterException
    {
        if (params instanceof PSSParameterSpec)
        {
            PSSParameterSpec newParamSpec = (PSSParameterSpec)params;
            
            if (originalSpec != null)
            {
                if (!JCEDigestUtil.isSameDigest(originalSpec.getDigestAlgorithm(), newParamSpec.getDigestAlgorithm()))
                {
                    throw new InvalidParameterException("parameter must be using " + originalSpec.getDigestAlgorithm());
                }
            }
            if (!newParamSpec.getMGFAlgorithm().equalsIgnoreCase("MGF1") && !newParamSpec.getMGFAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1.getId()))
            {
                throw new InvalidParameterException("unknown mask generation function specified");
            }
            
            if (!(newParamSpec.getMGFParameters() instanceof MGF1ParameterSpec))
            {
                throw new InvalidParameterException("unkown MGF parameters");
            }
            
            MGF1ParameterSpec   mgfParams = (MGF1ParameterSpec)newParamSpec.getMGFParameters();
            
            if (!JCEDigestUtil.isSameDigest(mgfParams.getDigestAlgorithm(), newParamSpec.getDigestAlgorithm()))
            {
                throw new InvalidParameterException("digest algorithm for MGF should be the same as for PSS parameters.");
            }
            
            Digest newDigest = JCEDigestUtil.getDigest(mgfParams.getDigestAlgorithm());
            
            if (newDigest == null)
            {
                throw new InvalidParameterException("no match on MGF digest algorithm: "+ mgfParams.getDigestAlgorithm());
            }

            this.engineParams = null;
            this.paramSpec = newParamSpec;
            this.mgfDigest = newDigest;
            this.saltLength = paramSpec.getSaltLength();
            this.trailer = getTrailer(paramSpec.getTrailerField());

            setupContentDigest();
        }
        else
        {
            throw new InvalidParameterException("Only PSSParameterSpec supported");
        }
    }

    protected AlgorithmParameters engineGetParameters() 
    {
        if (engineParams == null)
        {
            if (paramSpec != null)
            {
                try
                {
                    engineParams = AlgorithmParameters.getInstance("PSS", BouncyCastleProvider.PROVIDER_NAME);
                    engineParams.init(paramSpec);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.toString());
                }
            }
        }

        return engineParams;
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
    
    protected Object engineGetParameter(
        String param)
    {
        throw new UnsupportedOperationException("engineGetParameter unsupported");
    }

    static public class nonePSS
        extends JDKPSSSigner
    {
        public nonePSS()
        {
            super(new RSABlindedEngine(), null, true);
        }
    }

    static public class PSSwithRSA
        extends JDKPSSSigner
    {
        public PSSwithRSA()
        {
            super(new RSABlindedEngine(), null);
        }
    }
    
    static public class SHA1withRSA
        extends JDKPSSSigner
    {
        public SHA1withRSA()
        {
            super(new RSABlindedEngine(), PSSParameterSpec.DEFAULT);
        }
    }

    static public class SHA224withRSA
        extends JDKPSSSigner
    {
        public SHA224withRSA()
        {
            super(new RSABlindedEngine(), new PSSParameterSpec("SHA-224", "MGF1", new MGF1ParameterSpec("SHA-224"), 28, 1));
        }
    }
    
    static public class SHA256withRSA
        extends JDKPSSSigner
    {
        public SHA256withRSA()
        {
            super(new RSABlindedEngine(), new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1));
        }
    }

    static public class SHA384withRSA
        extends JDKPSSSigner
    {
        public SHA384withRSA()
        {
            super(new RSABlindedEngine(), new PSSParameterSpec("SHA-384", "MGF1", new MGF1ParameterSpec("SHA-384"), 48, 1));
        }
    }

    static public class SHA512withRSA
        extends JDKPSSSigner
    {
        public SHA512withRSA()
        {
            super(new RSABlindedEngine(), new PSSParameterSpec("SHA-512", "MGF1", new MGF1ParameterSpec("SHA-512"), 64, 1));
        }
    }

    private class NullPssDigest
        implements Digest
    {
        private ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        private Digest baseDigest;
        private boolean oddTime = true;

        public NullPssDigest(Digest mgfDigest)
        {
            this.baseDigest = mgfDigest;
        }

        public String getAlgorithmName()
        {
            return "NULL";
        }

        public int getDigestSize()
        {
            return baseDigest.getDigestSize();
        }

        public void update(byte in)
        {
            bOut.write(in);
        }

        public void update(byte[] in, int inOff, int len)
        {
            bOut.write(in, inOff, len);
        }

        public int doFinal(byte[] out, int outOff)
        {
            byte[] res = bOut.toByteArray();

            if (oddTime)
            {
                System.arraycopy(res, 0, out, outOff, res.length);
            }
            else
            {
                baseDigest.update(res, 0, res.length);

                baseDigest.doFinal(out, outOff);
            }

            reset();

            oddTime = !oddTime;

            return res.length;
        }

        public void reset()
        {
            bOut.reset();
            baseDigest.reset();
        }
    }
}
