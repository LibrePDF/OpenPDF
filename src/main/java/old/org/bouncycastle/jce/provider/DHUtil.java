package old.org.bouncycastle.jce.provider;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.DHParameters;
import old.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.DHPublicKeyParameters;

/**
 * utility class for converting jce/jca DH objects
 * objects into their org.bouncycastle.crypto counterparts.
 */
public class DHUtil
{
    static public AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey    key)
        throws InvalidKeyException
    {
        if (key instanceof DHPublicKey)
        {
            DHPublicKey    k = (DHPublicKey)key;

            return new DHPublicKeyParameters(k.getY(),
                new DHParameters(k.getParams().getP(), k.getParams().getG(), null, k.getParams().getL()));
        }

        throw new InvalidKeyException("can't identify DH public key.");
    }

    static public AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey    key)
        throws InvalidKeyException
    {
        if (key instanceof DHPrivateKey)
        {
            DHPrivateKey    k = (DHPrivateKey)key;

            return new DHPrivateKeyParameters(k.getX(),
                new DHParameters(k.getParams().getP(), k.getParams().getG(), null, k.getParams().getL()));
        }
                        
        throw new InvalidKeyException("can't identify DH private key.");
    }
}
