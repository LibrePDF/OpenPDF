package old.org.bouncycastle.cert.crmf;

import java.io.IOException;

import old.org.bouncycastle.asn1.cms.EnvelopedData;
import old.org.bouncycastle.asn1.crmf.CRMFObjectIdentifiers;
import old.org.bouncycastle.asn1.crmf.EncKeyWithID;
import old.org.bouncycastle.asn1.crmf.EncryptedKey;
import old.org.bouncycastle.asn1.crmf.PKIArchiveOptions;
import old.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.cms.CMSEnvelopedData;
import old.org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.CMSProcessableByteArray;
import old.org.bouncycastle.cms.RecipientInfoGenerator;
import old.org.bouncycastle.operator.OutputEncryptor;

/**
 * Builder for a PKIArchiveControl structure.
 */
public class PKIArchiveControlBuilder
{
    private CMSEnvelopedDataGenerator envGen;
    private CMSProcessableByteArray keyContent;

    /**
     * Basic constructor - specify the contents of the PKIArchiveControl structure.
     *
     * @param privateKeyInfo the private key to be archived.
     * @param generalName the general name to be associated with the private key.
     */
    public PKIArchiveControlBuilder(PrivateKeyInfo privateKeyInfo, GeneralName generalName)
    {
        EncKeyWithID encKeyWithID = new EncKeyWithID(privateKeyInfo, generalName);

        try
        {
            this.keyContent = new CMSProcessableByteArray(CRMFObjectIdentifiers.id_ct_encKeyWithID, encKeyWithID.getEncoded());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("unable to encode key and general name info");
        }

        this.envGen = new CMSEnvelopedDataGenerator();
    }

    /**
     * Add a recipient generator to this control.
     *
     * @param recipientGen recipient generator created for a specific recipient.
     * @return this builder object.
     */
    public PKIArchiveControlBuilder addRecipientGenerator(RecipientInfoGenerator recipientGen)
    {
        envGen.addRecipientInfoGenerator(recipientGen);

        return this;
    }

    /**
     * Build the PKIArchiveControl using the passed in encryptor to encrypt its contents.
     *
     * @param contentEncryptor a suitable content encryptor.
     * @return a PKIArchiveControl object.
     * @throws CMSException in the event the build fails.
     */
    public PKIArchiveControl build(OutputEncryptor contentEncryptor)
        throws CMSException
    {
        CMSEnvelopedData envContent = envGen.generate(keyContent, contentEncryptor);

        EnvelopedData envD = EnvelopedData.getInstance(envContent.getContentInfo().getContent());

        return new PKIArchiveControl(new PKIArchiveOptions(new EncryptedKey(envD)));
    }
}