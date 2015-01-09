package old.org.bouncycastle.jce.provider;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Hashtable;

import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.crypto.params.DESParameters;
import old.org.bouncycastle.util.Strings;

/**
 * Diffie-Hellman key agreement. There's actually a better way of doing this
 * if you are using long term public keys, see the light-weight version for
 * details.
 */
public class JCEDHKeyAgreement
    extends KeyAgreementSpi
{
    private BigInteger      x;
    private BigInteger      p;
    private BigInteger      g;
    private BigInteger      result;

    private static final Hashtable algorithms = new Hashtable();

    static
    {
        Integer i64 = new Integer(64);
        Integer i192 = new Integer(192);
        Integer i128 = new Integer(128);
        Integer i256 = new Integer(256);

        algorithms.put("DES", i64);
        algorithms.put("DESEDE", i192);
        algorithms.put("BLOWFISH", i128);
        algorithms.put("AES", i256);
    }

    private byte[] bigIntToBytes(
        BigInteger    r)
    {
        byte[]    tmp = r.toByteArray();
        
        if (tmp[0] == 0)
        {
            byte[]    ntmp = new byte[tmp.length - 1];
            
            System.arraycopy(tmp, 1, ntmp, 0, ntmp.length);
            return ntmp;
        }
        
        return tmp;
    }
    
    protected Key engineDoPhase(
        Key     key,
        boolean lastPhase) 
        throws InvalidKeyException, IllegalStateException
    {
        if (x == null)
        {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }

        if (!(key instanceof DHPublicKey))
        {
            throw new InvalidKeyException("DHKeyAgreement doPhase requires DHPublicKey");
        }
        DHPublicKey pubKey = (DHPublicKey)key;

        if (!pubKey.getParams().getG().equals(g) || !pubKey.getParams().getP().equals(p))
        {
            throw new InvalidKeyException("DHPublicKey not for this KeyAgreement!");
        }

        if (lastPhase)
        {
            result = ((DHPublicKey)key).getY().modPow(x, p);
            return null;
        }
        else
        {
            result = ((DHPublicKey)key).getY().modPow(x, p);
        }

        return new JCEDHPublicKey(result, pubKey.getParams());
    }

    protected byte[] engineGenerateSecret() 
        throws IllegalStateException
    {
        if (x == null)
        {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }

        return bigIntToBytes(result);
    }

    protected int engineGenerateSecret(
        byte[]  sharedSecret,
        int     offset) 
        throws IllegalStateException, ShortBufferException
    {
        if (x == null)
        {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }

        byte[]  secret = bigIntToBytes(result);

        if (sharedSecret.length - offset < secret.length)
        {
            throw new ShortBufferException("DHKeyAgreement - buffer too short");
        }

        System.arraycopy(secret, 0, sharedSecret, offset, secret.length);

        return secret.length;
    }

    protected SecretKey engineGenerateSecret(
        String algorithm) 
    {
        if (x == null)
        {
            throw new IllegalStateException("Diffie-Hellman not initialised.");
        }

        String algKey = Strings.toUpperCase(algorithm);
        byte[] res = bigIntToBytes(result);

        if (algorithms.containsKey(algKey))
        {
            Integer length = (Integer)algorithms.get(algKey);

            byte[] key = new byte[length.intValue() / 8];
            System.arraycopy(res, 0, key, 0, key.length);

            if (algKey.startsWith("DES"))
            {
                DESParameters.setOddParity(key);
            }
            
            return new SecretKeySpec(key, algorithm);
        }

        return new SecretKeySpec(res, algorithm);
    }

    protected void engineInit(
        Key                     key,
        AlgorithmParameterSpec  params,
        SecureRandom            random) 
        throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        if (!(key instanceof DHPrivateKey))
        {
            throw new InvalidKeyException("DHKeyAgreement requires DHPrivateKey for initialisation");
        }
        DHPrivateKey    privKey = (DHPrivateKey)key;

        if (params != null)
        {
            if (!(params instanceof DHParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("DHKeyAgreement only accepts DHParameterSpec");
            }
            DHParameterSpec p = (DHParameterSpec)params;

            this.p = p.getP();
            this.g = p.getG();
        }
        else
        {
            this.p = privKey.getParams().getP();
            this.g = privKey.getParams().getG();
        }

        this.x = this.result = privKey.getX();
    }

    protected void engineInit(
        Key             key,
        SecureRandom    random) 
        throws InvalidKeyException
    {
        if (!(key instanceof DHPrivateKey))
        {
            throw new InvalidKeyException("DHKeyAgreement requires DHPrivateKey");
        }

        DHPrivateKey    privKey = (DHPrivateKey)key;

        this.p = privKey.getParams().getP();
        this.g = privKey.getParams().getG();
        this.x = this.result = privKey.getX();
    }
}
