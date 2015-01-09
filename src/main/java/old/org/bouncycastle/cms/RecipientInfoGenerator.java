package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.cms.RecipientInfo;
import old.org.bouncycastle.operator.GenericKey;

public interface RecipientInfoGenerator
{
    RecipientInfo generate(GenericKey contentEncryptionKey)
        throws CMSException;
}
