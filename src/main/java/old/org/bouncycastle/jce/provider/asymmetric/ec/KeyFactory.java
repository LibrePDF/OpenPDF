package old.org.bouncycastle.jce.provider.asymmetric.ec;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import old.org.bouncycastle.jce.provider.JCEECPrivateKey;
import old.org.bouncycastle.jce.provider.JCEECPublicKey;
import old.org.bouncycastle.jce.provider.JDKKeyFactory;
import old.org.bouncycastle.jce.provider.ProviderUtil;
import old.org.bouncycastle.jce.spec.ECParameterSpec;
import old.org.bouncycastle.jce.spec.ECPrivateKeySpec;
import old.org.bouncycastle.jce.spec.ECPublicKeySpec;

public class KeyFactory
    extends JDKKeyFactory
{
    String algorithm;

    KeyFactory(
        String algorithm)
    {
        this.algorithm = algorithm;
    }

    protected Key engineTranslateKey(
        Key    key)
        throws InvalidKeyException
    {
        if (key instanceof ECPublicKey)
        {
            return new JCEECPublicKey((ECPublicKey)key);
        }
        else if (key instanceof ECPrivateKey)
        {
            return new JCEECPrivateKey((ECPrivateKey)key);
        }

        throw new InvalidKeyException("key type unknown");
    }

    protected KeySpec engineGetKeySpec(
        Key    key,
        Class    spec)
    throws InvalidKeySpecException
    {
       if (spec.isAssignableFrom(PKCS8EncodedKeySpec.class) && key.getFormat().equals("PKCS#8"))
       {
               return new PKCS8EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(X509EncodedKeySpec.class) && key.getFormat().equals("X.509"))
       {
               return new X509EncodedKeySpec(key.getEncoded());
       }
       else if (spec.isAssignableFrom(java.security.spec.ECPublicKeySpec.class) && key instanceof ECPublicKey)
       {
           ECPublicKey k = (ECPublicKey)key;
           if (k.getParams() != null)
           {
               return new java.security.spec.ECPublicKeySpec(k.getW(), k.getParams());
           }
           else
           {
               ECParameterSpec implicitSpec = ProviderUtil.getEcImplicitlyCa();

               return new java.security.spec.ECPublicKeySpec(k.getW(), EC5Util.convertSpec(EC5Util.convertCurve(implicitSpec.getCurve(), implicitSpec.getSeed()), implicitSpec));
           }
       }
       else if (spec.isAssignableFrom(java.security.spec.ECPrivateKeySpec.class) && key instanceof ECPrivateKey)
       {
           ECPrivateKey k = (ECPrivateKey)key;

           if (k.getParams() != null)
           {
               return new java.security.spec.ECPrivateKeySpec(k.getS(), k.getParams());
           }
           else
           {
               ECParameterSpec implicitSpec = ProviderUtil.getEcImplicitlyCa();

               return new java.security.spec.ECPrivateKeySpec(k.getS(), EC5Util.convertSpec(EC5Util.convertCurve(implicitSpec.getCurve(), implicitSpec.getSeed()), implicitSpec)); 
           }
       }

       throw new RuntimeException("not implemented yet " + key + " " + spec);
    }

    protected PrivateKey engineGeneratePrivate(
        KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (keySpec instanceof PKCS8EncodedKeySpec)
        {
            try
            {
                JCEECPrivateKey key = (JCEECPrivateKey)JDKKeyFactory.createPrivateKeyFromDERStream(
                    ((PKCS8EncodedKeySpec)keySpec).getEncoded());

                return new JCEECPrivateKey(algorithm, key);
            }
            catch (Exception e)
            {
                throw new InvalidKeySpecException(e.toString());
            }
        }
        else if (keySpec instanceof ECPrivateKeySpec)
        {
            return new JCEECPrivateKey(algorithm, (ECPrivateKeySpec)keySpec);
        }
        else if (keySpec instanceof java.security.spec.ECPrivateKeySpec)
        {
            return new JCEECPrivateKey(algorithm, (java.security.spec.ECPrivateKeySpec)keySpec);
        }

        throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
    }

    protected PublicKey engineGeneratePublic(
        KeySpec keySpec)
        throws InvalidKeySpecException
    {
        if (keySpec instanceof X509EncodedKeySpec)
        {
            try
            {
                JCEECPublicKey key = (JCEECPublicKey)JDKKeyFactory.createPublicKeyFromDERStream(
                    ((X509EncodedKeySpec)keySpec).getEncoded());

                return new JCEECPublicKey(algorithm, key);
            }
            catch (Exception e)
            {
                throw new InvalidKeySpecException(e.toString());
            }
        }
        else if (keySpec instanceof ECPublicKeySpec)
        {
            return new JCEECPublicKey(algorithm, (ECPublicKeySpec)keySpec);
        }
        else if (keySpec instanceof java.security.spec.ECPublicKeySpec)
        {
            return new JCEECPublicKey(algorithm, (java.security.spec.ECPublicKeySpec)keySpec);
        }

        throw new InvalidKeySpecException("Unknown KeySpec type: " + keySpec.getClass().getName());
    }

    public static class EC
        extends KeyFactory
    {
        public EC()
        {
            super("EC");
        }
    }

    public static class ECDSA
        extends KeyFactory
    {
        public ECDSA()
        {
            super("ECDSA");
        }
    }

    public static class ECGOST3410
        extends KeyFactory
    {
        public ECGOST3410()
        {
            super("ECGOST3410");
        }
    }

    public static class ECDH
        extends KeyFactory
    {
        public ECDH()
        {
            super("ECDH");
        }
    }

    public static class ECDHC
        extends KeyFactory
    {
        public ECDHC()
        {
            super("ECDHC");
        }
    }

    public static class ECMQV
        extends KeyFactory
    {
        public ECMQV()
        {
            super("ECMQV");
        }
    }
}