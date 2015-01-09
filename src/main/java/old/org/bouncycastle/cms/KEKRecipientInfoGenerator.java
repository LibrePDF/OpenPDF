package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.cms.KEKIdentifier;
import old.org.bouncycastle.asn1.cms.KEKRecipientInfo;
import old.org.bouncycastle.asn1.cms.RecipientInfo;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OperatorException;
import old.org.bouncycastle.operator.SymmetricKeyWrapper;

public abstract class KEKRecipientInfoGenerator
    implements RecipientInfoGenerator
{
    private final KEKIdentifier kekIdentifier;

    protected final SymmetricKeyWrapper wrapper;

    protected KEKRecipientInfoGenerator(KEKIdentifier kekIdentifier, SymmetricKeyWrapper wrapper)
    {
        this.kekIdentifier = kekIdentifier;
        this.wrapper = wrapper;
    }

    public final RecipientInfo generate(GenericKey contentEncryptionKey)
        throws CMSException
    {
        try
        {
            ASN1OctetString encryptedKey = new DEROctetString(wrapper.generateWrappedKey(contentEncryptionKey));

            return new RecipientInfo(new KEKRecipientInfo(kekIdentifier, wrapper.getAlgorithmIdentifier(), encryptedKey));
        }
        catch (OperatorException e)
        {
            throw new CMSException("exception wrapping content key: " + e.getMessage(), e);
        }
    }
}