package old.org.bouncycastle.jce.provider.symmetric;

import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.crypto.KeyGenerationParameters;
import old.org.bouncycastle.crypto.engines.DESedeEngine;
import old.org.bouncycastle.crypto.engines.DESedeWrapEngine;
import old.org.bouncycastle.crypto.engines.RFC3211WrapEngine;
import old.org.bouncycastle.crypto.generators.DESedeKeyGenerator;
import old.org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import old.org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import old.org.bouncycastle.crypto.macs.CMac;
import old.org.bouncycastle.crypto.modes.CBCBlockCipher;
import old.org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import old.org.bouncycastle.jce.provider.JCEBlockCipher;
import old.org.bouncycastle.jce.provider.JCEKeyGenerator;
import old.org.bouncycastle.jce.provider.JCEMac;
import old.org.bouncycastle.jce.provider.JCESecretKeyFactory;
import old.org.bouncycastle.jce.provider.WrapCipherSpi;

public final class DESede
{
    private DESede()
    {
    }

    static public class ECB
        extends JCEBlockCipher
    {
        public ECB()
        {
            super(new DESedeEngine());
        }
    }

    static public class CBC
        extends JCEBlockCipher
    {
        public CBC()
        {
            super(new CBCBlockCipher(new DESedeEngine()), 64);
        }
    }

    /**
     * DESede   CFB8
     */
    public static class DESedeCFB8
        extends JCEMac
    {
        public DESedeCFB8()
        {
            super(new CFBBlockCipherMac(new DESedeEngine()));
        }
    }

    /**
     * DESede64
     */
    public static class DESede64
        extends JCEMac
    {
        public DESede64()
        {
            super(new CBCBlockCipherMac(new DESedeEngine(), 64));
        }
    }

    /**
     * DESede64with7816-4Padding
     */
    public static class DESede64with7816d4
        extends JCEMac
    {
        public DESede64with7816d4()
        {
            super(new CBCBlockCipherMac(new DESedeEngine(), 64, new ISO7816d4Padding()));
        }
    }
    
    public static class CBCMAC
        extends JCEMac
    {
        public CBCMAC()
        {
            super(new CBCBlockCipherMac(new DESedeEngine()));
        }
    }

    static public class CMAC
        extends JCEMac
    {
        public CMAC()
        {
            super(new CMac(new DESedeEngine()));
        }
    }

    public static class Wrap
        extends WrapCipherSpi
    {
        public Wrap()
        {
            super(new DESedeWrapEngine());
        }
    }

    public static class RFC3211
        extends WrapCipherSpi
    {
        public RFC3211()
        {
            super(new RFC3211WrapEngine(new DESedeEngine()), 8);
        }
    }

  /**
     * DESede - the default for this is to generate a key in
     * a-b-a format that's 24 bytes long but has 16 bytes of
     * key material (the first 8 bytes is repeated as the last
     * 8 bytes). If you give it a size, you'll get just what you
     * asked for.
     */
    public static class KeyGenerator
        extends JCEKeyGenerator
    {
        private boolean     keySizeSet = false;

        public KeyGenerator()
        {
            super("DESede", 192, new DESedeKeyGenerator());
        }

        protected void engineInit(
            int             keySize,
            SecureRandom random)
        {
            super.engineInit(keySize, random);
            keySizeSet = true;
        }

        protected SecretKey engineGenerateKey()
        {
            if (uninitialised)
            {
                engine.init(new KeyGenerationParameters(new SecureRandom(), defaultKeySize));
                uninitialised = false;
            }

            //
            // if no key size has been defined generate a 24 byte key in
            // the a-b-a format
            //
            if (!keySizeSet)
            {
                byte[]     k = engine.generateKey();

                System.arraycopy(k, 0, k, 16, 8);

                return new SecretKeySpec(k, algName);
            }
            else
            {
                return new SecretKeySpec(engine.generateKey(), algName);
            }
        }
    }

    /**
     * generate a desEDE key in the a-b-c format.
     */
    public static class KeyGenerator3
        extends JCEKeyGenerator
    {
        public KeyGenerator3()
        {
            super("DESede3", 192, new DESedeKeyGenerator());
        }
    }

    static public class KeyFactory
        extends JCESecretKeyFactory
    {
        public KeyFactory()
        {
            super("DESede", null);
        }

        protected KeySpec engineGetKeySpec(
            SecretKey key,
            Class keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec == null)
            {
                throw new InvalidKeySpecException("keySpec parameter is null");
            }
            if (key == null)
            {
                throw new InvalidKeySpecException("key parameter is null");
            }

            if (SecretKeySpec.class.isAssignableFrom(keySpec))
            {
                return new SecretKeySpec(key.getEncoded(), algName);
            }
            else if (DESedeKeySpec.class.isAssignableFrom(keySpec))
            {
                byte[]  bytes = key.getEncoded();

                try
                {
                    if (bytes.length == 16)
                    {
                        byte[]  longKey = new byte[24];

                        System.arraycopy(bytes, 0, longKey, 0, 16);
                        System.arraycopy(bytes, 0, longKey, 16, 8);

                        return new DESedeKeySpec(longKey);
                    }
                    else
                    {
                        return new DESedeKeySpec(bytes);
                    }
                }
                catch (Exception e)
                {
                    throw new InvalidKeySpecException(e.toString());
                }
            }

            throw new InvalidKeySpecException("Invalid KeySpec");
        }

        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof DESedeKeySpec)
            {
                DESedeKeySpec desKeySpec = (DESedeKeySpec)keySpec;
                return new SecretKeySpec(desKeySpec.getKey(), "DESede");
            }

            return super.engineGenerateSecret(keySpec);
        }
    }

    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("Cipher.DESEDE", "org.bouncycastle.jce.provider.symmetric.DESede$ECB");
            put("Cipher." + PKCSObjectIdentifiers.des_EDE3_CBC, "org.bouncycastle.jce.provider.symmetric.DESede$CBC");
            put("Cipher." + OIWObjectIdentifiers.desCBC, "org.bouncycastle.jce.provider.symmetric.DESede$CBC");
            put("Cipher.DESEDEWRAP", "org.bouncycastle.jce.provider.symmetric.DESede$Wrap");
            put("Cipher." + PKCSObjectIdentifiers.id_alg_CMS3DESwrap, "org.bouncycastle.jce.provider.symmetric.DESede$Wrap");
            put("Cipher.DESEDERFC3211WRAP", "org.bouncycastle.jce.provider.symmetric.DESede$RFC3211");

            put("KeyGenerator.DESEDE", "org.bouncycastle.jce.provider.symmetric.DESede$KeyGenerator");
            put("KeyGenerator." + PKCSObjectIdentifiers.des_EDE3_CBC, "org.bouncycastle.jce.provider.symmetric.DESede$KeyGenerator3");
            put("KeyGenerator.DESEDEWRAP", "org.bouncycastle.jce.provider.symmetric.DESede$KeyGenerator");

            put("SecretKeyFactory.DESEDE", "org.bouncycastle.jce.provider.symmetric.DESede$KeyFactory");

            put("Mac.DESEDECMAC", "org.bouncycastle.jce.provider.symmetric.DESede$CMAC");
            put("Mac.DESEDEMAC", "org.bouncycastle.jce.provider.symmetric.DESede$CBCMAC");
            put("Alg.Alias.Mac.DESEDE", "DESEDEMAC");

            put("Mac.DESEDEMAC/CFB8", "org.bouncycastle.jce.provider.symmetric.DESede$DESedeCFB8");
            put("Alg.Alias.Mac.DESEDE/CFB8", "DESEDEMAC/CFB8");

            put("Mac.DESEDEMAC64", "org.bouncycastle.jce.provider.symmetric.DESede$DESede64");
            put("Alg.Alias.Mac.DESEDE64", "DESEDEMAC64");

            put("Mac.DESEDEMAC64WITHISO7816-4PADDING", "org.bouncycastle.jce.provider.symmetric.DESede$DESede64with7816d4");
            put("Alg.Alias.Mac.DESEDE64WITHISO7816-4PADDING", "DESEDEMAC64WITHISO7816-4PADDING");
            put("Alg.Alias.Mac.DESEDEISO9797ALG1MACWITHISO7816-4PADDING", "DESEDEMAC64WITHISO7816-4PADDING");
            put("Alg.Alias.Mac.DESEDEISO9797ALG1WITHISO7816-4PADDING", "DESEDEMAC64WITHISO7816-4PADDING");
        }
    }
}
