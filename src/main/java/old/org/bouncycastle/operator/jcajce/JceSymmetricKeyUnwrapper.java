package old.org.bouncycastle.operator.jcajce;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OperatorException;
import old.org.bouncycastle.operator.SymmetricKeyUnwrapper;

public class JceSymmetricKeyUnwrapper
    extends SymmetricKeyUnwrapper
{
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private SecretKey secretKey;

    public JceSymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, SecretKey secretKey)
    {
        super(algorithmIdentifier);

        this.secretKey = secretKey;
    }

    public JceSymmetricKeyUnwrapper setProvider(Provider provider)
    {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JceSymmetricKeyUnwrapper setProvider(String providerName)
    {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public GenericKey generateUnwrappedKey(AlgorithmIdentifier encryptedKeyAlgorithm, byte[] encryptedKey)
        throws OperatorException
    {
        try
        {
            Cipher keyCipher = helper.createSymmetricWrapper(this.getAlgorithmIdentifier().getAlgorithm());

            keyCipher.init(Cipher.UNWRAP_MODE, secretKey);

            return new GenericKey(keyCipher.unwrap(encryptedKey, encryptedKeyAlgorithm.getAlgorithm().getId(), Cipher.SECRET_KEY));
        }
        catch (InvalidKeyException e)
        {
            throw new OperatorException("key invalid in message.", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new OperatorException("can't find algorithm.", e);
        }
    }
}
