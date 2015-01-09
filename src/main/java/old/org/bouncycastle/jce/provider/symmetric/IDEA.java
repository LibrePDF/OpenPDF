package old.org.bouncycastle.jce.provider.symmetric;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;

import javax.crypto.spec.IvParameterSpec;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.misc.IDEACBCPar;
import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.IDEAEngine;
import old.org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import old.org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import old.org.bouncycastle.crypto.modes.CBCBlockCipher;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEMac;
import old.org.bouncycastle.jce.provider.JCESecretKeyFactory;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameterGenerator;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameters;

public final class IDEA
{
    private IDEA()
    {
    }
    
    public static class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new IDEAEngine());
        }
    }

    public static class CBC
       extends JCEBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new IDEAEngine()), 64);
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("IDEA", 128, new CipherKeyGenerator());
        }
    }

    public static class PBEWithSHAAndIDEAKeyGen
       extends JCESecretKeyFactory.PBEKeyFactory
    {
       public PBEWithSHAAndIDEAKeyGen()
       {
           super("PBEwithSHAandIDEA-CBC", null, true, PKCS12, SHA1, 128, 64);
       }
    }

    static public class PBEWithSHAAndIDEA
        extends JCEBlockCipher
    {
        public PBEWithSHAAndIDEA()
        {
            super(new CBCBlockCipher(new IDEAEngine()));
        }
    }

    public static class AlgParamGen
        extends JDKAlgorithmParameterGenerator
    {
        protected void engineInit(
            AlgorithmParameterSpec genParamSpec,
            SecureRandom random)
            throws InvalidAlgorithmParameterException
        {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for IDEA parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            byte[] iv = new byte[8];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("IDEA", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new IvParameterSpec(iv));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class AlgParams
        extends JDKAlgorithmParameters
    {
        private byte[]  iv;

        protected byte[] engineGetEncoded()
            throws IOException
        {
            return engineGetEncoded("ASN.1");
        }

        protected byte[] engineGetEncoded(
            String format)
            throws IOException
        {
            if (isASN1FormatString(format))
            {
                return new IDEACBCPar(engineGetEncoded("RAW")).getEncoded();
            }

            if (format.equals("RAW"))
            {
                byte[]  tmp = new byte[iv.length];

                System.arraycopy(iv, 0, tmp, 0, iv.length);
                return tmp;
            }

            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(
            Class paramSpec)
            throws InvalidParameterSpecException
        {
            if (paramSpec == IvParameterSpec.class)
            {
                return new IvParameterSpec(iv);
            }

            throw new InvalidParameterSpecException("unknown parameter spec passed to IV parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException
        {
            if (!(paramSpec instanceof IvParameterSpec))
            {
                throw new InvalidParameterSpecException("IvParameterSpec required to initialise a IV parameters algorithm parameters object");
            }

            this.iv = ((IvParameterSpec)paramSpec).getIV();
        }

        protected void engineInit(
            byte[] params)
            throws IOException
        {
            this.iv = new byte[params.length];

            System.arraycopy(params, 0, iv, 0, iv.length);
        }

        protected void engineInit(
            byte[] params,
            String format)
            throws IOException
        {
            if (format.equals("RAW"))
            {
                engineInit(params);
                return;
            }
            if (format.equals("ASN.1"))
            {
                ASN1InputStream aIn = new ASN1InputStream(params);
                IDEACBCPar      oct = new IDEACBCPar((ASN1Sequence)aIn.readObject());

                engineInit(oct.getIV());
                return;
            }

            throw new IOException("Unknown parameters format in IV parameters object");
        }

        protected String engineToString()
        {
            return "IDEA Parameters";
        }
    }
    
    public static class Mac
        extends JCEMac
    {
        public Mac()
        {
            super(new CBCBlockCipherMac(new IDEAEngine()));
        }
    }

    public static class CFB8Mac
        extends JCEMac
    {
        public CFB8Mac()
        {
            super(new CFBBlockCipherMac(new IDEAEngine()));
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("AlgorithmParameterGenerator.IDEA", "org.bouncycastle.jce.provider.symmetric.IDEA$AlgParamGen");
            put("AlgorithmParameterGenerator.1.3.6.1.4.1.188.7.1.1.2", "org.bouncycastle.jce.provider.symmetric.IDEA$AlgParamGen");
            put("AlgorithmParameters.IDEA", "org.bouncycastle.jce.provider.symmetric.IDEA$AlgParams");
            put("AlgorithmParameters.1.3.6.1.4.1.188.7.1.1.2", "org.bouncycastle.jce.provider.symmetric.IDEA$AlgParams");
            put("Alg.Alias.AlgorithmParameters.PBEWITHSHAANDIDEA", "PKCS12PBE");
            put("Alg.Alias.AlgorithmParameters.PBEWITHSHAANDIDEA", "PKCS12PBE");
            put("Alg.Alias.AlgorithmParameters.PBEWITHSHAANDIDEA-CBC", "PKCS12PBE");
            put("Cipher.IDEA", "org.bouncycastle.jce.provider.symmetric.IDEA$ECB");
            put("Cipher.1.3.6.1.4.1.188.7.1.1.2", "org.bouncycastle.jce.provider.symmetric.IDEA$CBC");
            put("Cipher.PBEWITHSHAANDIDEA-CBC", "org.bouncycastle.jce.provider.symmetric.IDEA$PBEWithSHAAndIDEA");
            put("KeyGenerator.IDEA", "org.bouncycastle.jce.provider.symmetric.IDEA$KeyGen");
            put("KeyGenerator.1.3.6.1.4.1.188.7.1.1.2", "org.bouncycastle.jce.provider.symmetric.IDEA$KeyGen");
            put("SecretKeyFactory.PBEWITHSHAANDIDEA-CBC", "org.bouncycastle.jce.provider.symmetric.IDEA$PBEWithSHAAndIDEAKeyGen");
            put("Mac.IDEAMAC", "org.bouncycastle.jce.provider.symmetric.IDEA$Mac");
            put("Alg.Alias.Mac.IDEA", "IDEAMAC");
            put("Mac.IDEAMAC/CFB8", "org.bouncycastle.jce.provider.symmetric.IDEA$CFB8Mac");
            put("Alg.Alias.Mac.IDEA/CFB8", "IDEAMAC/CFB8");
        }
    }
}
