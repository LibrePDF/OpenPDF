package old.org.bouncycastle.jce.provider.symmetric;

import java.util.HashMap;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.SkipjackEngine;
import old.org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import old.org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEMac;
import old.org.bouncycastle.jce.provider.JDKAlgorithmParameters;

public final class Skipjack
{
    private Skipjack()
    {
    }
    
    public static class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new SkipjackEngine());
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("Skipjack", 80, new CipherKeyGenerator());
        }
    }

    public static class AlgParams
        extends JDKAlgorithmParameters.IVAlgorithmParameters
    {
        protected String engineToString()
        {
            return "Skipjack IV";
        }
    }

    public static class Mac
        extends JCEMac
    {
        public Mac()
        {
            super(new CBCBlockCipherMac(new SkipjackEngine()));
        }
    }

    public static class MacCFB8
        extends JCEMac
    {
        public MacCFB8()
        {
            super(new CFBBlockCipherMac(new SkipjackEngine()));
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.SKIPJACK", "org.bouncycastle.jce.provider.symmetric.Skipjack$ECB");
            put("KeyGenerator.SKIPJACK", "org.bouncycastle.jce.provider.symmetric.Skipjack$KeyGen");
            put("AlgorithmParameters.SKIPJACK", "org.bouncycastle.jce.provider.symmetric.Skipjack$AlgParams");
            put("Mac.SKIPJACKMAC", "org.bouncycastle.jce.provider.symmetric.Skipjack$Mac");
            put("Alg.Alias.Mac.SKIPJACK", "SKIPJACKMAC");
            put("Mac.SKIPJACKMAC/CFB8", "org.bouncycastle.jce.provider.symmetric.Skipjack$MacCFB8");
            put("Alg.Alias.Mac.SKIPJACK/CFB8", "SKIPJACKMAC/CFB8");
        }
    }
}
