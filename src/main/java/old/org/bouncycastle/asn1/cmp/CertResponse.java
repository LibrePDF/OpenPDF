package old.org.bouncycastle.asn1.cmp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class CertResponse
    extends ASN1Encodable
{
    private DERInteger certReqId;
    private PKIStatusInfo status;
    private CertifiedKeyPair certifiedKeyPair;
    private ASN1OctetString rspInfo;

    private CertResponse(ASN1Sequence seq)
    {
        certReqId = DERInteger.getInstance(seq.getObjectAt(0));
        status = PKIStatusInfo.getInstance(seq.getObjectAt(1));

        if (seq.size() >= 3)
        {
            if (seq.size() == 3)
            {
                DEREncodable o = seq.getObjectAt(2);
                if (o instanceof ASN1OctetString)
                {
                    rspInfo = ASN1OctetString.getInstance(o);
                }
                else
                {
                    certifiedKeyPair = CertifiedKeyPair.getInstance(o);
                }
            }
            else
            {
                certifiedKeyPair = CertifiedKeyPair.getInstance(seq.getObjectAt(2));
                rspInfo = ASN1OctetString.getInstance(seq.getObjectAt(3));
            }
        }
    }

    public static CertResponse getInstance(Object o)
    {
        if (o instanceof CertResponse)
        {
            return (CertResponse)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new CertResponse((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public CertResponse(
        DERInteger certReqId,
        PKIStatusInfo status)
    {
        this(certReqId, status, null, null);
    }

    public CertResponse(
        DERInteger certReqId,
        PKIStatusInfo status,
        CertifiedKeyPair certifiedKeyPair,
        ASN1OctetString rspInfo)
    {
        if (certReqId == null)
        {
            throw new IllegalArgumentException("'certReqId' cannot be null");
        }
        if (status == null)
        {
            throw new IllegalArgumentException("'status' cannot be null");
        }
        this.certReqId = certReqId;
        this.status = status;
        this.certifiedKeyPair = certifiedKeyPair;
        this.rspInfo = rspInfo;
    }

    public DERInteger getCertReqId()
    {
        return certReqId;
    }

    public PKIStatusInfo getStatus()
    {
        return status;
    }

    public CertifiedKeyPair getCertifiedKeyPair()
    {
        return certifiedKeyPair;
    }

    /**
     * <pre>
     * CertResponse ::= SEQUENCE {
     *                            certReqId           INTEGER,
     *                            -- to match this response with corresponding request (a value
     *                            -- of -1 is to be used if certReqId is not specified in the
     *                            -- corresponding request)
     *                            status              PKIStatusInfo,
     *                            certifiedKeyPair    CertifiedKeyPair    OPTIONAL,
     *                            rspInfo             OCTET STRING        OPTIONAL
     *                            -- analogous to the id-regInfo-utf8Pairs string defined
     *                            -- for regInfo in CertReqMsg [CRMF]
     *             }
     * </pre> 
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(certReqId);
        v.add(status);

        if (certifiedKeyPair != null)
        {
            v.add(certifiedKeyPair);
        }

        if (rspInfo != null)
        {
            v.add(rspInfo);
        }
        
        return new DERSequence(v);
    }
}
