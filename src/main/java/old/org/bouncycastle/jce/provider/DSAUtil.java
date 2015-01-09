package old.org.bouncycastle.jce.provider;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.DSAParameters;
import old.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.DSAPublicKeyParameters;

/**
 * utility class for converting jce/jca DSA objects
 * objects into their org.bouncycastle.crypto counterparts.
 */
public class DSAUtil
{
    static public AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey    key)
        throws InvalidKeyException
    {
        if (key instanceof DSAPublicKey)
        {
            DSAPublicKey    k = (DSAPublicKey)key;

            return new DSAPublicKeyParameters(k.getY(),
                new DSAParameters(k.getParams().getP(), k.getParams().getQ(), k.getParams().getG()));
        }

        throw new InvalidKeyException("can't identify DSA public key: " + key.getClass().getName());
    }

    static public AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey    key)
        throws InvalidKeyException
    {
        if (key instanceof DSAPrivateKey)
        {
            DSAPrivateKey    k = (DSAPrivateKey)key;

            return new DSAPrivateKeyParameters(k.getX(),
                new DSAParameters(k.getParams().getP(), k.getParams().getQ(), k.getParams().getG()));
        }
                        
        throw new InvalidKeyException("can't identify DSA private key.");
    }
}
