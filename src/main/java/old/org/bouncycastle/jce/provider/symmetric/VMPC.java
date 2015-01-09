package old.org.bouncycastle.jce.provider.symmetric;

import java.util.HashMap;

import old.org.bouncycastle.crypto.CipherKeyGenerator;
import old.org.bouncycastle.crypto.engines.VMPCEngine;
import old.org.bouncycastle.crypto.macs.VMPCMac;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEMac;
import old.org.bouncycastle.jce.provider.JCEStreamCipher;

public final class VMPC
{
    private VMPC()
    {
    }
    
    public static class Base
        extends JCEStreamCipher
    {
        public Base()
        {
            super(new VMPCEngine(), 16);
        }
    }

    public static class KeyGen
        extends JCEKeyGenerator
    {
        public KeyGen()
        {
            super("VMPC", 128, new CipherKeyGenerator());
        }
    }

    public static class Mac
        extends JCEMac
    {
        public Mac()
        {
            super(new VMPCMac());
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.VMPC", "org.bouncycastle.jce.provider.symmetric.VMPC$Base");
            put("KeyGenerator.VMPC", "org.bouncycastle.jce.provider.symmetric.VMPC$KeyGen");
            put("Mac.VMPCMAC", "org.bouncycastle.jce.provider.symmetric.VMPC$Mac");
            put("Alg.Alias.Mac.VMPC", "VMPCMAC");
            put("Alg.Alias.Mac.VMPC-MAC", "VMPCMAC");
        }
    }
}
