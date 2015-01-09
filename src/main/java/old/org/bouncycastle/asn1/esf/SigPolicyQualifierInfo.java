package old.org.bouncycastle.asn1.esf;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;

public class SigPolicyQualifierInfo
    extends ASN1Encodable
{
    private DERObjectIdentifier  sigPolicyQualifierId;
    private DEREncodable         sigQualifier;

    public SigPolicyQualifierInfo(
        DERObjectIdentifier   sigPolicyQualifierId,
        DEREncodable          sigQualifier)
    {
        this.sigPolicyQualifierId = sigPolicyQualifierId;
        this.sigQualifier = sigQualifier;
    }

    public SigPolicyQualifierInfo(
        ASN1Sequence seq)
    {
        sigPolicyQualifierId = DERObjectIdentifier.getInstance(seq.getObjectAt(0));
        sigQualifier = seq.getObjectAt(1);
    }

    public static SigPolicyQualifierInfo getInstance(
        Object obj)
    {
        if (obj == null || obj instanceof SigPolicyQualifierInfo)
        {
            return (SigPolicyQualifierInfo) obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new SigPolicyQualifierInfo((ASN1Sequence) obj);
        }

        throw new IllegalArgumentException(
                "unknown object in 'SigPolicyQualifierInfo' factory: "
                        + obj.getClass().getName() + ".");
    }

    public ASN1ObjectIdentifier getSigPolicyQualifierId()
    {
        return new ASN1ObjectIdentifier(sigPolicyQualifierId.getId());
    }

    public DEREncodable getSigQualifier()
    {
        return sigQualifier;
    }

    /**
     * <pre>
     * SigPolicyQualifierInfo ::= SEQUENCE {
     *    sigPolicyQualifierId SigPolicyQualifierId,
     *    sigQualifier ANY DEFINED BY sigPolicyQualifierId }
     *
     * SigPolicyQualifierId ::= OBJECT IDENTIFIER
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(sigPolicyQualifierId);
        v.add(sigQualifier);

        return new DERSequence(v);
    }
}
