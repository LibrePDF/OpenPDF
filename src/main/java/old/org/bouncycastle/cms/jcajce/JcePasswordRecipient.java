package old.org.bouncycastle.cms.jcajce;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.PasswordRecipient;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;

/**
 * the RecipientInfo class for a recipient who has been sent a message
 * encrypted using a password.
 */
public abstract class JcePasswordRecipient
    implements PasswordRecipient
{
    private int schemeID = PasswordRecipient.PKCS5_SCHEME2_UTF8;
    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceHelper());
    private char[] password;

    JcePasswordRecipient(
        char[] password)
    {
        this.password = password;
    }

    public JcePasswordRecipient setPasswordConversionScheme(int schemeID)
    {
        this.schemeID = schemeID;

        return this;
    }

    public JcePasswordRecipient setProvider(Provider provider)
    {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    public JcePasswordRecipient setProvider(String providerName)
    {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    protected Key extractSecretKey(AlgorithmIdentifier keyEncryptionAlgorithm, AlgorithmIdentifier contentEncryptionAlgorithm, byte[] derivedKey, byte[] encryptedContentEncryptionKey)
        throws CMSException
    {
        Cipher keyEncryptionCipher = helper.createRFC3211Wrapper(keyEncryptionAlgorithm.getAlgorithm());

        try
        {
            IvParameterSpec ivSpec = new IvParameterSpec(ASN1OctetString.getInstance(keyEncryptionAlgorithm.getParameters()).getOctets());

            keyEncryptionCipher.init(Cipher.UNWRAP_MODE, new SecretKeySpec(derivedKey, keyEncryptionCipher.getAlgorithm()), ivSpec);

            return keyEncryptionCipher.unwrap(encryptedContentEncryptionKey, contentEncryptionAlgorithm.getAlgorithm().getId(), Cipher.SECRET_KEY);
        }
        catch (GeneralSecurityException e)
        {
            throw new CMSException("cannot process content encryption key: " + e.getMessage(), e);
        }
    }

    public int getPasswordConversionScheme()
    {
        return schemeID;
    }

    public char[] getPassword()
    {
        return password;
    }
}
