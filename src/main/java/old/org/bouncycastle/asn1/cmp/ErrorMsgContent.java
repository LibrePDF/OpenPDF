package old.org.bouncycastle.asn1.cmp;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class ErrorMsgContent
    extends ASN1Encodable
{
    private PKIStatusInfo pkiStatusInfo;
    private DERInteger errorCode;
    private PKIFreeText errorDetails;

    private ErrorMsgContent(ASN1Sequence seq)
    {
        Enumeration en = seq.getObjects();

        pkiStatusInfo = PKIStatusInfo.getInstance(en.nextElement());

        while (en.hasMoreElements())
        {
            Object o = en.nextElement();

            if (o instanceof DERInteger)
            {
                errorCode = DERInteger.getInstance(o);
            }
            else
            {
                errorDetails = PKIFreeText.getInstance(o);
            }
        }
    }

    public static ErrorMsgContent getInstance(Object o)
    {
        if (o instanceof ErrorMsgContent)
        {
            return (ErrorMsgContent)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new ErrorMsgContent((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public ErrorMsgContent(PKIStatusInfo pkiStatusInfo)
    {
        this(pkiStatusInfo, null, null);
    }

    public ErrorMsgContent(
        PKIStatusInfo pkiStatusInfo,
        DERInteger errorCode,
        PKIFreeText errorDetails)
    {
        if (pkiStatusInfo == null)
        {
            throw new IllegalArgumentException("'pkiStatusInfo' cannot be null");
        }

        this.pkiStatusInfo = pkiStatusInfo;
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    public PKIStatusInfo getPKIStatusInfo()
    {
        return pkiStatusInfo;
    }

    public DERInteger getErrorCode()
    {
        return errorCode;
    }

    public PKIFreeText getErrorDetails()
    {
        return errorDetails;
    }

    /**
     * <pre>
     * ErrorMsgContent ::= SEQUENCE {
     *                        pKIStatusInfo          PKIStatusInfo,
     *                        errorCode              INTEGER           OPTIONAL,
     *                        -- implementation-specific error codes
     *                        errorDetails           PKIFreeText       OPTIONAL
     *                        -- implementation-specific error details
     * }
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(pkiStatusInfo);
        addOptional(v, errorCode);
        addOptional(v, errorDetails);

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
