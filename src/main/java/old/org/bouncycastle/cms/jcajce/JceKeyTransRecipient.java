package old.org.bouncycastle.cms.jcajce;

import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.KeyTransRecipient;
import old.org.bouncycastle.jcajce.DefaultJcaJceHelper;
import old.org.bouncycastle.jcajce.NamedJcaJceHelper;
import old.org.bouncycastle.jcajce.ProviderJcaJceHelper;
import old.org.bouncycastle.operator.AsymmetricKeyUnwrapper;
import old.org.bouncycastle.operator.OperatorException;

public abstract class JceKeyTransRecipient
    implements KeyTransRecipient
{
    private PrivateKey recipientKey;

    protected EnvelopedDataHelper helper = new EnvelopedDataHelper(new DefaultJcaJceHelper());
    protected EnvelopedDataHelper contentHelper = helper;

    public JceKeyTransRecipient(PrivateKey recipientKey)
    {
        this.recipientKey = recipientKey;
    }

    /**
     * Set the provider to use for key recovery and content processing.
     *
     * @param provider provider to use.
     * @return this recipient.
     */
    public JceKeyTransRecipient setProvider(Provider provider)
    {
        this.helper = new EnvelopedDataHelper(new ProviderJcaJceHelper(provider));
        this.contentHelper = helper;

        return this;
    }

    /**
     * Set the provider to use for key recovery and content processing.
     *
     * @param providerName the name of the provider to use.
     * @return this recipient.
     */
    public JceKeyTransRecipient setProvider(String providerName)
    {
        this.helper = new EnvelopedDataHelper(new NamedJcaJceHelper(providerName));
        this.contentHelper = helper;

        return this;
    }

    /**
     * Set the provider to use for content processing.
     *
     * @param provider the provider to use.
     * @return this recipient.
     */
    public JceKeyTransRecipient setContentProvider(Provider provider)
    {
        this.contentHelper = new EnvelopedDataHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    /**
     * Set the provider to use for content processing.
     *
     * @param providerName the name of the provider to use.
     * @return this recipient.
     */
    public JceKeyTransRecipient setContentProvider(String providerName)
    {
        this.contentHelper = new EnvelopedDataHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    protected Key extractSecretKey(AlgorithmIdentifier keyEncryptionAlgorithm, AlgorithmIdentifier encryptedKeyAlgorithm, byte[] encryptedEncryptionKey)
        throws CMSException
    {
        AsymmetricKeyUnwrapper unwrapper = helper.createAsymmetricUnwrapper(keyEncryptionAlgorithm, recipientKey);

        try
        {
            return CMSUtils.getJceKey(unwrapper.generateUnwrappedKey(encryptedKeyAlgorithm, encryptedEncryptionKey));
        }
        catch (OperatorException e)
        {
            throw new CMSException("exception unwrapping key: " + e.getMessage(), e);
        }
    }
}
