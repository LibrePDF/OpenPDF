package old.org.bouncycastle.jce.provider.symmetric;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;

import javax.crypto.spec.IvParameterSpec;

import old.org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.SEEDEngine;
import old.org.bouncycastle.crypto.engines.SEEDWrapEngine;
import old.org.bouncycastle.crypto.modes.CBCBlockCipher;
import old.org.bouncycastle.jce.provider.BouncyCastleProvider;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameterGenerator;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameters;
import old.org.bouncycastle.jce.provider.WrapCipherSpi;

public final class SEED
{
    private SEED()
    {
    }
    
    public static class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new SEEDEngine());
        }
    }

    public static class CBC
       extends JCEBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new SEEDEngine()), 128);
        }
    }

    public static class Wrap
        extends WrapCipherSpi
    {
        public Wrap()
        {
            super(new SEEDWrapEngine());
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("SEED", 128, new CipherKeyGenerator());
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
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for SEED parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            byte[] iv = new byte[16];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("SEED", BouncyCastleProvider.PROVIDER_NAME);
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
        extends JDKAlgorithmParameters.IVAlgorithmParameters
    {
        protected String engineToString()
        {
            return "SEED IV";
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("AlgorithmParameters.SEED", "org.bouncycastle.jce.provider.symmetric.SEED$AlgParams");
            put("Alg.Alias.AlgorithmParameters." + KISAObjectIdentifiers.id_seedCBC, "SEED");

            put("AlgorithmParameterGenerator.SEED", "org.bouncycastle.jce.provider.symmetric.SEED$AlgParamGen");
            put("Alg.Alias.AlgorithmParameterGenerator." + KISAObjectIdentifiers.id_seedCBC, "SEED");

            put("Cipher.SEED", "org.bouncycastle.jce.provider.symmetric.SEED$ECB");
            put("Cipher." + KISAObjectIdentifiers.id_seedCBC, "org.bouncycastle.jce.provider.symmetric.SEED$CBC");

            put("Cipher.SEEDWRAP", "org.bouncycastle.jce.provider.symmetric.SEED$Wrap");
            put("Alg.Alias.Cipher." + KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap, "SEEDWRAP");

            put("KeyGenerator.SEED", "org.bouncycastle.jce.provider.symmetric.SEED$KeyGen");
            put("KeyGenerator." + KISAObjectIdentifiers.id_seedCBC, "org.bouncycastle.jce.provider.symmetric.SEED$KeyGen");
            put("KeyGenerator." + KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap, "org.bouncycastle.jce.provider.symmetric.SEED$KeyGen");
        }
    }
}
