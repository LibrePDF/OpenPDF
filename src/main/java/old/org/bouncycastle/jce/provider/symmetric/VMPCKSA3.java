package old.org.bouncycastle.jce.provider.symmetric;

import java.util.HashMap;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.VMPCKSA3Engine;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEStreamCipher;

public final class VMPCKSA3
{
    private VMPCKSA3()
    {
    }
    
    public static class Base
        extends JCEStreamCipher
    {
        public Base()
        {
            super(new VMPCKSA3Engine(), 16);
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("VMPC-KSA3", 128, new CipherKeyGenerator());
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.VMPC-KSA3", "org.bouncycastle.jce.provider.symmetric.VMPCKSA3$Base");
            put("KeyGenerator.VMPC-KSA3", "org.bouncycastle.jce.provider.symmetric.VMPCKSA3$KeyGen");
        }
    }
}
