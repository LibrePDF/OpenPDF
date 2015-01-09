package old.org.bouncycastle.jce.provider;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;

import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.ElGamalParameters;
import old.org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import old.org.bouncycastle.jce.interfaces.ElGamalPrivateKey;
import old.org.bouncycastle.jce.interfaces.ElGamalPublicKey;

/**
 * utility class for converting jce/jca ElGamal objects
 * objects into their org.bouncycastle.crypto counterparts.
 */
public class ElGamalUtil
{
    static public AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ElGamalPublicKey)
        {
            ElGamalPublicKey    k = (ElGamalPublicKey)key;

            return new ElGamalPublicKeyParameters(k.getY(),
                new ElGamalParameters(k.getParameters().getP(), k.getParameters().getG()));
        }
        else if (key instanceof DHPublicKey)
        {
            DHPublicKey    k = (DHPublicKey)key;

            return new ElGamalPublicKeyParameters(k.getY(),
                new ElGamalParameters(k.getParams().getP(), k.getParams().getG()));
        }

        throw new InvalidKeyException("can't identify public key for El Gamal.");
    }

    static public AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey    key)
        throws InvalidKeyException
    {
        if (key instanceof ElGamalPrivateKey)
        {
            ElGamalPrivateKey    k = (ElGamalPrivateKey)key;

            return new ElGamalPrivateKeyParameters(k.getX(),
                new ElGamalParameters(k.getParameters().getP(), k.getParameters().getG()));
        }
        else if (key instanceof DHPrivateKey)
        {
            DHPrivateKey    k = (DHPrivateKey)key;

            return new ElGamalPrivateKeyParameters(k.getX(),
                new ElGamalParameters(k.getParams().getP(), k.getParams().getG()));
        }
                        
        throw new InvalidKeyException("can't identify private key for El Gamal.");
    }
}
