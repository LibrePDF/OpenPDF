package old.org.bouncycastle.asn1.crmf;

import old.org.bouncycastle.asn1.ASN1Choice;
import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERTaggedObject;
import old.org.bouncycastle.asn1.cms.EnvelopedData;

public class EncryptedKey
    extends ASN1Encodable
    implements ASN1Choice
{
    private EnvelopedData envelopedData;
    private EncryptedValue encryptedValue;

    public static EncryptedKey getInstance(Object o)
    {
        if (o instanceof EncryptedKey)
        {
            return (EncryptedKey)o;
        }
        else if (o instanceof ASN1TaggedObject)
        {
            return new EncryptedKey(EnvelopedData.getInstance((ASN1TaggedObject)o, false));
        }
        else if (o instanceof EncryptedValue)
        {
            return new EncryptedKey((EncryptedValue)o);
        }
        else
        {
            return new EncryptedKey(EncryptedValue.getInstance(o));
        }
    }

    public EncryptedKey(EnvelopedData envelopedData)
    {
        this.envelopedData = envelopedData;
    }

    public EncryptedKey(EncryptedValue encryptedValue)
    {
        this.encryptedValue = encryptedValue;
    }

    public boolean isEncryptedValue()
    {
        return encryptedValue != null;
    }

    public ASN1Encodable getValue()
    {
        if (encryptedValue != null)
        {
            return encryptedValue;
        }

        return envelopedData;
    }

    /**
     * <pre>
     *    EncryptedKey ::= CHOICE {
     *        encryptedValue        EncryptedValue, -- deprecated
     *        envelopedData     [0] EnvelopedData }
     *        -- The encrypted private key MUST be placed in the envelopedData
     *        -- encryptedContentInfo encryptedContent OCTET STRING.
     * </pre>
     */
    public DERObject toASN1Object()
    {
        if (encryptedValue != null)
        {
            return encryptedValue.toASN1Object();
        }

        return new DERTaggedObject(false, 0, envelopedData);
    }
}
