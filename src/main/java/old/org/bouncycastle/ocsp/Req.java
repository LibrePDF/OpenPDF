package old.org.bouncycastle.ocsp;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.ocsp.Request;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

public class Req
    implements java.security.cert.X509Extension
{
    private Request req;

    public Req(
        Request req)
    {
        this.req = req;
    }

    public CertificateID getCertID()
    {
        return new CertificateID(req.getReqCert());
    }

    public X509Extensions getSingleRequestExtensions()
    {
        return req.getSingleRequestExtensions();
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
        X509Extensions  extensions = this.getSingleRequestExtensions();
        
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
        X509Extensions exts = this.getSingleRequestExtensions();

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
