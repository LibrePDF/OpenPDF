package old.org.bouncycastle.jce.provider.symmetric;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;

import javax.crypto.spec.IvParameterSpec;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.RC532Engine;
import old.org.bouncycastle.crypto.engines.RC564Engine;
import old.org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import old.org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import old.org.bouncycastle.crypto.modes.CBCBlockCipher;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEMac;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameterGenerator;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameters;

public final class RC5
{
    private RC5()
    {
    }

    /**
     * RC5
     */
    public static class ECB32
        extends JCEBlockCipher
    {
        public ECB32()
        {
            super(new RC532Engine());
        }
    }

    /**
     * RC564
     */
    public static class ECB64
        extends JCEBlockCipher
    {
        public ECB64()
        {
            super(new RC564Engine());
        }
    }

    public static class CBC32
       extends JCEBlockCipher
    {
        public CBC32()
        {
            super(new CBCBlockCipher(new RC532Engine()), 64);
        }
    }

    public static class KeyGen32
        extends JCEKeyGenerator
    {
        public KeyGen32()
        {
            super("RC5", 128, new CipherKeyGenerator());
        }
    }

    /**
     * RC5
     */
    public static class KeyGen64
        extends JCEKeyGenerator
    {
        public KeyGen64()
        {
            super("RC5-64", 256, new CipherKeyGenerator());
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
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for RC5 parameter generation.");
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
                params = AlgorithmParameters.getInstance("RC5", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new IvParameterSpec(iv));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class Mac32
        extends JCEMac
    {
        public Mac32()
        {
            super(new CBCBlockCipherMac(new RC532Engine()));
        }
    }

    public static class CFB8Mac32
        extends JCEMac
    {
        public CFB8Mac32()
        {
            super(new CFBBlockCipherMac(new RC532Engine()));
        }
    }

    public static class AlgParams
        extends JDKAlgorithmParameters.IVAlgorithmParameters
    {
        protected String engineToString()
        {
            return "RC5 IV";
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.RC5", "org.bouncycastle.jce.provider.symmetric.RC5$ECB32");
            put("Alg.Alias.Cipher.RC5-32", "RC5");
            put("Cipher.RC5-64", "org.bouncycastle.jce.provider.symmetric.RC5$ECB64");
            put("KeyGenerator.RC5", "org.bouncycastle.jce.provider.symmetric.RC5$KeyGen32");
            put("Alg.Alias.KeyGenerator.RC5-32", "RC5");
            put("KeyGenerator.RC5-64", "org.bouncycastle.jce.provider.symmetric.RC5$KeyGen64");
            put("AlgorithmParameters.RC5", "org.bouncycastle.jce.provider.symmetric.RC5$AlgParams");
            put("AlgorithmParameters.RC5-64", "org.bouncycastle.jce.provider.symmetric.RC5$AlgParams");
            put("Mac.RC5MAC", "org.bouncycastle.jce.provider.symmetric.RC5$Mac32");
            put("Alg.Alias.Mac.RC5", "RC5MAC");
            put("Mac.RC5MAC/CFB8", "org.bouncycastle.jce.provider.symmetric.RC5$CFB8Mac32");
            put("Alg.Alias.Mac.RC5/CFB8", "RC5MAC/CFB8");
        }
    }
}
