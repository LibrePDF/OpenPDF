package old.org.bouncycastle.jce.provider.symmetric;

import java.util.HashMap;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.HC128Engine;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEStreamCipher;

public final class HC128
{
    private HC128()
    {
    }
    
    public static class Base
        extends JCEStreamCipher
    {
        public Base()
        {
            super(new HC128Engine(), 16);
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("HC128", 128, new CipherKeyGenerator());
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.HC128", "org.bouncycastle.jce.provider.symmetric.HC128$Base");
            put("KeyGenerator.HC128", "org.bouncycastle.jce.provider.symmetric.HC128$KeyGen");
        }
    }
}
