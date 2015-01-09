package old.org.bouncycastle.cert.ocsp;

import java.util.Date;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ocsp.CertStatus;
import old.org.bouncycastle.asn1.ocsp.RevokedInfo;
import old.org.bouncycastle.asn1.ocsp.SingleResponse;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

public class SingleResp
{
    private SingleResponse  resp;
    private X509Extensions extensions;

    public SingleResp(
        SingleResponse  resp)
    {
        this.resp = resp;
        this.extensions = resp.getSingleExtensions();
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
    public CertificateStatus getCertStatus()
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
        return OCSPUtils.extractDate(resp.getThisUpdate());
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

        return OCSPUtils.extractDate(resp.getNextUpdate());
    }

    public boolean hasExtensions()
    {
        return extensions != null;
    }

    public X509Extension getExtension(ASN1ObjectIdentifier oid)
    {
        if (extensions != null)
        {
            return extensions.getExtension(oid);
        }

        return null;
    }

    public List getExtensionOIDs()
    {
        return OCSPUtils.getExtensionOIDs(extensions);
    }

    public Set getCriticalExtensionOIDs()
    {
        return OCSPUtils.getCriticalExtensionOIDs(extensions);
    }

    public Set getNonCriticalExtensionOIDs()
    {
        return OCSPUtils.getNonCriticalExtensionOIDs(extensions);
    }
}
