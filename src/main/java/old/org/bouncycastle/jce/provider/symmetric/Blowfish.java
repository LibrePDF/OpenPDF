package old.org.bouncycastle.jce.provider.symmetric;

import java.util.HashMap;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.BlowfishEngine;
import old.org.bouncycastle.crypto.modes.CBCBlockCipher;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameters;

public final class Blowfish
{
    private Blowfish()
    {
    }
    
    public static class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new BlowfishEngine());
        }
    }

    public static class CBC
        extends JCEBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new BlowfishEngine()), 64);
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("Blowfish", 128, new CipherKeyGenerator());
        }
    }

    public static class AlgParams
        extends JDKAlgorithmParameters.IVAlgorithmParameters
    {
        protected String engineToString()
        {
            return "Blowfish IV";
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.BLOWFISH", "org.bouncycastle.jce.provider.symmetric.Blowfish$ECB");
            put("Cipher.1.3.6.1.4.1.3029.1.2", "org.bouncycastle.jce.provider.symmetric.Blowfish$CBC");
            put("KeyGenerator.BLOWFISH", "org.bouncycastle.jce.provider.symmetric.Blowfish$KeyGen");
            put("Alg.Alias.KeyGenerator.1.3.6.1.4.1.3029.1.2", "BLOWFISH");
            put("AlgorithmParameters.BLOWFISH", "org.bouncycastle.jce.provider.symmetric.Blowfish$AlgParams");
            put("Alg.Alias.AlgorithmParameters.1.3.6.1.4.1.3029.1.2", "BLOWFISH");
        }
    }
}
