package old.org.bouncycastle.ocsp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.ocsp.CertStatus;
import old.org.bouncycastle.asn1.ocsp.RevokedInfo;
import old.org.bouncycastle.asn1.ocsp.SingleResponse;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class SingleResp
    implements java.security.cert.X509Extension
{
    SingleResponse  resp;

    public SingleResp(
        SingleResponse  resp)
    {
        this.resp = resp;
    }

    public CertificateID getCertID()
    {
        return new CertificateID(resp.getCertID());
    }

    /**
     * Return the status object for the response - null indicates good.
     * 
     * @return the status object for the response, null if it is good.
     */
    public Object getCertStatus()
    {
        CertStatus  s = resp.getCertStatus();

        if (s.getTagNo() == 0)
        {
            return null;            // good
        }
        else if (s.getTagNo() == 1)
        {
            return new RevokedStatus(RevokedInfo.getInstance(s.getStatus()));
        }

        return new UnknownStatus();
    }

    public Date getThisUpdate()
    {
        try
        {
            return resp.getThisUpdate().getDate();
        }
        catch (ParseException e)
        {
            throw new IllegalStateException("ParseException: " + e.getMessage());
        }
    }

    /**
     * return the NextUpdate value - note: this is an optional field so may
     * be returned as null.
     *
     * @return nextUpdate, or null if not present.
     */
    public Date getNextUpdate()
    {
        if (resp.getNextUpdate() == null)
        {
            return null;
        }

        try
        {
            return resp.getNextUpdate().getDate();
        }
        catch (ParseException e)
        {
            throw new IllegalStateException("ParseException: " + e.getMessage());
        }
    }

    public X509Extensions getSingleExtensions()
    {
        return resp.getSingleExtensions();
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
        
        return extns != null && !extns.isEmpty();
    }

    private Set getExtensionOIDs(boolean critical)
    {
        Set             set = new HashSet();
        X509Extensions  extensions = this.getSingleExtensions();
        
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
        X509Extensions exts = this.getSingleExtensions();

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
