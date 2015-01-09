package old.org.bouncycastle.asn1.crmf;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class CertReqMsg
    extends ASN1Encodable
{
    private CertRequest certReq;
    private ProofOfPossession popo;
    private ASN1Sequence regInfo;

    private CertReqMsg(ASN1Sequence seq)
    {
        Enumeration en = seq.getObjects();

        certReq = CertRequest.getInstance(en.nextElement());
        while (en.hasMoreElements())
        {
            Object o = en.nextElement();

            if (o instanceof ASN1TaggedObject || o instanceof ProofOfPossession)
            {
                popo = ProofOfPossession.getInstance(o);
            }
            else
            {
                regInfo = ASN1Sequence.getInstance(o);
            }
        }
    }

    public static CertReqMsg getInstance(Object o)
    {
        if (o instanceof CertReqMsg)
        {
            return (CertReqMsg)o;
        }
        else if (o != null)
        {
            return new CertReqMsg(ASN1Sequence.getInstance(o));
        }

        return null;
    }

    /**
     * Creates a new CertReqMsg.
     * @param certReq CertRequest
     * @param popo may be null
     * @param regInfo may be null
     */
    public CertReqMsg(
        CertRequest certReq,
        ProofOfPossession popo,
        AttributeTypeAndValue[] regInfo)
    {
        if (certReq == null)
        {
            throw new IllegalArgumentException("'certReq' cannot be null");
        }

        this.certReq = certReq;
        this.popo = popo;

        if (regInfo != null)
        {
            this.regInfo = new DERSequence(regInfo);
        }
    }

    public CertRequest getCertReq()
    {
        return certReq;
    }

    /**
     * @deprecated use getPopo
     */
    public ProofOfPossession getPop()
    {
        return popo;
    }

    public ProofOfPossession getPopo()
    {
        return popo;
    }

    public AttributeTypeAndValue[] getRegInfo()
    {
        if (regInfo == null)
        {
            return null;
        }

        AttributeTypeAndValue[] results = new AttributeTypeAndValue[regInfo.size()];

        for (int i = 0; i != results.length; i++)
        {
            results[i] = AttributeTypeAndValue.getInstance(regInfo.getObjectAt(i));
        }

        return results;
    }

    /**
     * <pre>
     * CertReqMsg ::= SEQUENCE {
     *                    certReq   CertRequest,
     *                    pop       ProofOfPossession  OPTIONAL,
     *                    -- content depends upon key type
     *                    regInfo   SEQUENCE SIZE(1..MAX) OF AttributeTypeAndValue OPTIONAL }
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(certReq);

        addOptional(v, popo);
        addOptional(v, regInfo);

        return new DERSequence(v);
    }

    private void addOptional(ASN1EncodableVector v, ASN1Encodable obj)
    {
        if (obj != null)
        {
            v.add(obj);
        }
    }
}
