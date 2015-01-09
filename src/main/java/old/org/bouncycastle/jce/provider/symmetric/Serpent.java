package old.org.bouncycastle.jce.provider.symmetric;

import java.util.HashMap;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.SerpentEngine;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameters;

public final class Serpent
{
    private Serpent()
    {
    }
    
    public static class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new SerpentEngine());
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("Serpent", 192, new CipherKeyGenerator());
        }
    }

    public static class AlgParams
        extends JDKAlgorithmParameters.IVAlgorithmParameters
    {
        protected String engineToString()
        {
            return "Serpent IV";
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.Serpent", "org.bouncycastle.jce.provider.symmetric.Serpent$ECB");
            put("KeyGenerator.Serpent", "org.bouncycastle.jce.provider.symmetric.Serpent$KeyGen");
            put("AlgorithmParameters.Serpent", "org.bouncycastle.jce.provider.symmetric.Serpent$AlgParams");
        }
    }
}
