package old.org.bouncycastle.jce.provider;

import java.lang.reflect.Constructor;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactorySpi;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.params.DESParameters;
import old.org.bouncycastle.crypto.params.KeyParameter;
import old.org.bouncycastle.crypto.params.ParametersWithIV;

public class JCESecretKeyFactory
    extends SecretKeyFactorySpi
    implements PBE
{
    protected String                algName;
    protected DERObjectIdentifier   algOid;

    protected JCESecretKeyFactory(
        String               algName,
        DERObjectIdentifier  algOid)
    {
        this.algName = algName;
        this.algOid = algOid;
    }

    protected SecretKey engineGenerateSecret(
        KeySpec keySpec)
    throws InvalidKeySpecException
    {
        if (keySpec instanceof SecretKeySpec)
        {
            return (SecretKey)keySpec;
        }

        throw new InvalidKeySpecException("Invalid KeySpec");
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

        try
        {
            Class[] parameters = { byte[].class };

            Constructor c = keySpec.getConstructor(parameters);
            Object[]    p = new Object[1];

            p[0] = key.getEncoded();

            return (KeySpec)c.newInstance(p);
        }
        catch (Exception e)
        {
            throw new InvalidKeySpecException(e.toString());
        }
    }

    protected SecretKey engineTranslateKey(
        SecretKey key)
    throws InvalidKeyException
    {
        if (key == null)
        {
            throw new InvalidKeyException("key parameter is null");
        }
        
        if (!key.getAlgorithm().equalsIgnoreCase(algName))
        {
            throw new InvalidKeyException("Key not of type " + algName + ".");
        }

        return new SecretKeySpec(key.getEncoded(), algName);
    }

    /*
     * classes that inherit from us
     */
    
    static public class PBEKeyFactory
        extends JCESecretKeyFactory
    {
        private boolean forCipher;
        private int     scheme;
        private int     digest;
        private int     keySize;
        private int     ivSize;
        
        public PBEKeyFactory(
            String              algorithm,
            DERObjectIdentifier oid,
            boolean             forCipher,
            int                 scheme,
            int                 digest,
            int                 keySize,
            int                 ivSize)
        {
            super(algorithm, oid);
            
            this.forCipher = forCipher;
            this.scheme = scheme;
            this.digest = digest;
            this.keySize = keySize;
            this.ivSize = ivSize;
        }
    
        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
            throws InvalidKeySpecException
        {
            if (keySpec instanceof PBEKeySpec)
            {
                PBEKeySpec          pbeSpec = (PBEKeySpec)keySpec;
                CipherParameters    param;
                
                if (pbeSpec.getSalt() == null)
                {
                    return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, null);
                }
                
                if (forCipher)
                {
                    param = Util.makePBEParameters(pbeSpec, scheme, digest, keySize, ivSize);
                }
                else
                {
                    param = Util.makePBEMacParameters(pbeSpec, scheme, digest, keySize);
                }
                
                return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, param);
            }
            
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }

    static public class DESPBEKeyFactory
        extends JCESecretKeyFactory
    {
        private boolean forCipher;
        private int     scheme;
        private int     digest;
        private int     keySize;
        private int     ivSize;
        
        public DESPBEKeyFactory(
            String              algorithm,
            DERObjectIdentifier oid,
            boolean             forCipher,
            int                 scheme,
            int                 digest,
            int                 keySize,
            int                 ivSize)
        {
            super(algorithm, oid);
            
            this.forCipher = forCipher;
            this.scheme = scheme;
            this.digest = digest;
            this.keySize = keySize;
            this.ivSize = ivSize;
        }
    
        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof PBEKeySpec)
            {
                PBEKeySpec pbeSpec = (PBEKeySpec)keySpec;
                CipherParameters    param;
                
                if (pbeSpec.getSalt() == null)
                {
                    return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, null);
                }
                
                if (forCipher)
                {
                    param = Util.makePBEParameters(pbeSpec, scheme, digest, keySize, ivSize);
                }
                else
                {
                    param = Util.makePBEMacParameters(pbeSpec, scheme, digest, keySize);
                }

                KeyParameter kParam;
                if (param instanceof ParametersWithIV)
                {
                    kParam = (KeyParameter)((ParametersWithIV)param).getParameters();
                }
                else
                {
                    kParam = (KeyParameter)param;
                }

                DESParameters.setOddParity(kParam.getKey());

                return new JCEPBEKey(this.algName, this.algOid, scheme, digest, keySize, ivSize, pbeSpec, param);
            }
            
            throw new InvalidKeySpecException("Invalid KeySpec");
        }
    }
    
    static public class DES
        extends JCESecretKeyFactory
    {
        public DES()
        {
            super("DES", null);
        }

        protected SecretKey engineGenerateSecret(
            KeySpec keySpec)
        throws InvalidKeySpecException
        {
            if (keySpec instanceof DESKeySpec)
            {
                DESKeySpec desKeySpec = (DESKeySpec)keySpec;
                return new SecretKeySpec(desKeySpec.getKey(), "DES");
            }

            return super.engineGenerateSecret(keySpec);
        }
    }

    /**
     * PBEWithMD2AndDES
     */
    static public class PBEWithMD2AndDES
        extends DESPBEKeyFactory
    {
        public PBEWithMD2AndDES()
        {
            super("PBEwithMD2andDES", PKCSObjectIdentifiers.pbeWithMD2AndDES_CBC, true, PKCS5S1, MD2, 64, 64);
        }
    }

    /**
     * PBEWithMD2AndRC2
     */
    static public class PBEWithMD2AndRC2
        extends PBEKeyFactory
    {
        public PBEWithMD2AndRC2()
        {
            super("PBEwithMD2andRC2", PKCSObjectIdentifiers.pbeWithMD2AndRC2_CBC, true, PKCS5S1, MD2, 64, 64);
        }
    }

   /**
    * PBEWithMD5AndDES
    */
   static public class PBEWithMD5AndDES
       extends DESPBEKeyFactory
   {
       public PBEWithMD5AndDES()
       {
           super("PBEwithMD5andDES", PKCSObjectIdentifiers.pbeWithMD5AndDES_CBC, true, PKCS5S1, MD5, 64, 64);
       }
   }

   /**
    * PBEWithMD5AndRC2
    */
   static public class PBEWithMD5AndRC2
       extends PBEKeyFactory
   {
       public PBEWithMD5AndRC2()
       {
           super("PBEwithMD5andRC2", PKCSObjectIdentifiers.pbeWithMD5AndRC2_CBC, true, PKCS5S1, MD5, 64, 64);
       }
   }

   /**
    * PBEWithSHA1AndDES
    */
   static public class PBEWithSHA1AndDES
       extends DESPBEKeyFactory
   {
       public PBEWithSHA1AndDES()
       {
           super("PBEwithSHA1andDES", PKCSObjectIdentifiers.pbeWithSHA1AndDES_CBC, true, PKCS5S1, SHA1, 64, 64);
       }
   }

   /**
    * PBEWithSHA1AndRC2
    */
   static public class PBEWithSHA1AndRC2
       extends PBEKeyFactory
   {
       public PBEWithSHA1AndRC2()
       {
           super("PBEwithSHA1andRC2", PKCSObjectIdentifiers.pbeWithSHA1AndRC2_CBC, true, PKCS5S1, SHA1, 64, 64);
       }
   }

   /**
    * PBEWithSHAAnd3-KeyTripleDES-CBC
    */
   static public class PBEWithSHAAndDES3Key
       extends DESPBEKeyFactory
   {
       public PBEWithSHAAndDES3Key()
       {
           super("PBEwithSHAandDES3Key-CBC", PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, true, PKCS12, SHA1, 192, 64);
       }
   }

   /**
    * PBEWithSHAAnd2-KeyTripleDES-CBC
    */
   static public class PBEWithSHAAndDES2Key
       extends DESPBEKeyFactory
   {
       public PBEWithSHAAndDES2Key()
       {
           super("PBEwithSHAandDES2Key-CBC", PKCSObjectIdentifiers.pbeWithSHAAnd2_KeyTripleDES_CBC, true, PKCS12, SHA1, 128, 64);
       }
   }

   /**
    * PBEWithSHAAnd128BitRC2-CBC
    */
   static public class PBEWithSHAAnd128BitRC2
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd128BitRC2()
       {
           super("PBEwithSHAand128BitRC2-CBC", PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC2_CBC, true, PKCS12, SHA1, 128, 64);
       }
   }

   /**
    * PBEWithSHAAnd40BitRC2-CBC
    */
   static public class PBEWithSHAAnd40BitRC2
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd40BitRC2()
       {
           super("PBEwithSHAand40BitRC2-CBC", PKCSObjectIdentifiers.pbewithSHAAnd40BitRC2_CBC, true, PKCS12, SHA1, 40, 64);
       }
   }

   /**
    * PBEWithSHAAndTwofish-CBC
    */
   static public class PBEWithSHAAndTwofish
       extends PBEKeyFactory
   {
       public PBEWithSHAAndTwofish()
       {
           super("PBEwithSHAandTwofish-CBC", null, true, PKCS12, SHA1, 256, 128);
       }
   }
   
   /**
    * PBEWithSHAAnd128BitRC4
    */
   static public class PBEWithSHAAnd128BitRC4
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd128BitRC4()
       {
           super("PBEWithSHAAnd128BitRC4", PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4, true, PKCS12, SHA1, 128, 0);
       }
   }

   /**
    * PBEWithSHAAnd40BitRC4
    */
   static public class PBEWithSHAAnd40BitRC4
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd40BitRC4()
       {
           super("PBEWithSHAAnd128BitRC4", PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4, true, PKCS12, SHA1, 40, 0);
       }
   }
   
   /**
    * PBEWithHmacRIPEMD160
    */
   public static class PBEWithRIPEMD160
       extends PBEKeyFactory
   {
       public PBEWithRIPEMD160()
       {
           super("PBEwithHmacRIPEMD160", null, false, PKCS12, RIPEMD160, 160, 0);
       }
   }

   /**
    * PBEWithHmacSHA
    */
   public static class PBEWithSHA
       extends PBEKeyFactory
   {
       public PBEWithSHA()
       {
           super("PBEwithHmacSHA", null, false, PKCS12, SHA1, 160, 0);
       }
   }

   /**
    * PBEWithHmacTiger
    */
   public static class PBEWithTiger
       extends PBEKeyFactory
   {
       public PBEWithTiger()
       {
           super("PBEwithHmacTiger", null, false, PKCS12, TIGER, 192, 0);
       }
   }
   
   /**
    * PBEWithSHA1And128BitAES-BC
    */
   static public class PBEWithSHAAnd128BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd128BitAESBC()
       {
           super("PBEWithSHA1And128BitAES-CBC-BC", null, true, PKCS12, SHA1, 128, 128);
       }
   }
   
   /**
    * PBEWithSHA1And192BitAES-BC
    */
   static public class PBEWithSHAAnd192BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd192BitAESBC()
       {
           super("PBEWithSHA1And192BitAES-CBC-BC", null, true, PKCS12, SHA1, 192, 128);
       }
   }
   
   /**
    * PBEWithSHA1And256BitAES-BC
    */
   static public class PBEWithSHAAnd256BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHAAnd256BitAESBC()
       {
           super("PBEWithSHA1And256BitAES-CBC-BC", null, true, PKCS12, SHA1, 256, 128);
       }
   }
   
   /**
    * PBEWithSHA256And128BitAES-BC
    */
   static public class PBEWithSHA256And128BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHA256And128BitAESBC()
       {
           super("PBEWithSHA256And128BitAES-CBC-BC", null, true, PKCS12, SHA256, 128, 128);
       }
   }
   
   /**
    * PBEWithSHA256And192BitAES-BC
    */
   static public class PBEWithSHA256And192BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHA256And192BitAESBC()
       {
           super("PBEWithSHA256And192BitAES-CBC-BC", null, true, PKCS12, SHA256, 192, 128);
       }
   }
   
   /**
    * PBEWithSHA256And256BitAES-BC
    */
   static public class PBEWithSHA256And256BitAESBC
       extends PBEKeyFactory
   {
       public PBEWithSHA256And256BitAESBC()
       {
           super("PBEWithSHA256And256BitAES-CBC-BC", null, true, PKCS12, SHA256, 256, 128);
       }
   }
   
   /**
    * PBEWithMD5And128BitAES-OpenSSL
    */
   static public class PBEWithMD5And128BitAESCBCOpenSSL
       extends PBEKeyFactory
   {
       public PBEWithMD5And128BitAESCBCOpenSSL()
       {
           super("PBEWithMD5And128BitAES-CBC-OpenSSL", null, true, OPENSSL, MD5, 128, 128);
       }
   }
   
   /**
    * PBEWithMD5And192BitAES-OpenSSL
    */
   static public class PBEWithMD5And192BitAESCBCOpenSSL
       extends PBEKeyFactory
   {
       public PBEWithMD5And192BitAESCBCOpenSSL()
       {
           super("PBEWithMD5And192BitAES-CBC-OpenSSL", null, true, OPENSSL, MD5, 192, 128);
       }
   }
   
   /**
    * PBEWithMD5And256BitAES-OpenSSL
    */
   static public class PBEWithMD5And256BitAESCBCOpenSSL
       extends PBEKeyFactory
   {
       public PBEWithMD5And256BitAESCBCOpenSSL()
       {
           super("PBEWithMD5And256BitAES-CBC-OpenSSL", null, true, OPENSSL, MD5, 256, 128);
       }
   }
}
