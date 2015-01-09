package old.org.bouncycastle.cert.ocsp;

import java.util.Date;

import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ocsp.ResponseData;
import old.org.bouncycastle.asn1.ocsp.SingleResponse;
import old.org.bouncycastle.asn1.x509.X509Extensions;

public class RespData
{
    private ResponseData    data;

    public RespData(
        ResponseData    data)
    {
        this.data = data;
    }

    public int getVersion()
    {
        return data.getVersion().getValue().intValue() + 1;
    }

    public RespID getResponderId()
    {
        return new RespID(data.getResponderID());
    }

    public Date getProducedAt()
    {
        return OCSPUtils.extractDate(data.getProducedAt());
    }

    public SingleResp[] getResponses()
    {
        ASN1Sequence    s = data.getResponses();
        SingleResp[]    rs = new SingleResp[s.size()];

        for (int i = 0; i != rs.length; i++)
        {
            rs[i] = new SingleResp(SingleResponse.getInstance(s.getObjectAt(i)));
        }

        return rs;
    }

    public X509Extensions getResponseExtensions()
    {
        return data.getResponseExtensions();
    }
}
