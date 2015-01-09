package old.org.bouncycastle.ocsp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.ocsp.ResponseData;
import old.org.bouncycastle.asn1.ocsp.SingleResponse;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class RespData
    implements java.security.cert.X509Extension
{
    ResponseData    data;

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
        try
        {
            return data.getProducedAt().getDate();
        }
        catch (ParseException e)
        {
            throw new IllegalStateException("ParseException:" + e.getMessage());
        }
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
    
    /**
     * RFC 2650 doesn't specify any critical extensions so we return true
     * if any are encountered.
     * 
     * @return true if any critical extensions are present.
     */
    public boolean hasUnsupportedCriticalExtension()
    {
        Set extns = getCriticalExtensionOIDs();
        if (extns != null && !extns.isEmpty())
        {
            return true;
        }

        return false;
    }

    private Set getExtensionOIDs(boolean critical)
    {
        Set             set = new HashSet();
        X509Extensions  extensions = this.getResponseExtensions();
        
        if (extensions != null)
        {
            Enumeration     e = extensions.oids();
    
            while (e.hasMoreElements())
            {
                DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                X509Extension       ext = extensions.getExtension(oid);
    
                if (critical == ext.isCritical())
                {
                    set.add(oid.getId());
                }
            }
        }

        return set;
    }

    public Set getCriticalExtensionOIDs()
    {
        return getExtensionOIDs(true);
    }

    public Set getNonCriticalExtensionOIDs()
    {
        return getExtensionOIDs(false);
    }

    public byte[] getExtensionValue(String oid)
    {
        X509Extensions exts = this.getResponseExtensions();

        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new DERObjectIdentifier(oid));

            if (ext != null)
            {
                try
                {
                    return ext.getValue().getEncoded(ASN1Encodable.DER);
                }
                catch (Exception e)
                {
                    throw new RuntimeException("error encoding " + e.toString());
                }
            }
        }

        return null;
    }
}
