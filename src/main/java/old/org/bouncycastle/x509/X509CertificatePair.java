package old.org.bouncycastle.x509;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.x509.CertificatePair;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.jce.provider.X509CertificateObject;

/**
 * This class contains a cross certificate pair. Cross certificates pairs may
 * contain two cross signed certificates from two CAs. A certificate from the
 * other CA to this CA is contained in the forward certificate, the certificate
 * from this CA to the other CA is contained in the reverse certificate.
 */
public class X509CertificatePair
{
    private X509Certificate forward;
    private X509Certificate reverse;

    /**
     * Constructor.
     *
     * @param forward Certificate from the other CA to this CA.
     * @param reverse Certificate from this CA to the other CA.
     */
    public X509CertificatePair(
        X509Certificate forward,
        X509Certificate reverse)
    {
        this.forward = forward;
        this.reverse = reverse;
    }

    /**
     * Constructor from a ASN.1 CertificatePair structure.
     *
     * @param pair The <code>CertificatePair</code> ASN.1 object.
     */
    public X509CertificatePair(
        CertificatePair pair)
        throws CertificateParsingException
    {
        if (pair.getForward() != null)
        {
            this.forward = new X509CertificateObject(pair.getForward());
        }
        if (pair.getReverse() != null)
        {
            this.reverse = new X509CertificateObject(pair.getReverse());
        }
    }
    
    public byte[] getEncoded()
        throws CertificateEncodingException
    {
        X509CertificateStructure f = null;
        X509CertificateStructure r = null;
        try
        {
            if (forward != null)
            {
                f = X509CertificateStructure.getInstance(new ASN1InputStream(
                    forward.getEncoded()).readObject());
                if (f == null)
                {
                    throw new CertificateEncodingException("unable to get encoding for forward");
                }
            }
            if (reverse != null)
            {
                r = X509CertificateStructure.getInstance(new ASN1InputStream(
                    reverse.getEncoded()).readObject());
                if (r == null)
                {
                    throw new CertificateEncodingException("unable to get encoding for reverse");
                }
            }
            return new CertificatePair(f, r).getDEREncoded();
        }
        catch (IllegalArgumentException e)
        {
            throw new ExtCertificateEncodingException(e.toString(), e);
        }
        catch (IOException e)
        {
            throw new ExtCertificateEncodingException(e.toString(), e);
        }
    }

    /**
     * Returns the certificate from the other CA to this CA.
     *
     * @return Returns the forward certificate.
     */
    public X509Certificate getForward()
    {
        return forward;
    }

    /**
     * Return the certificate from this CA to the other CA.
     *
     * @return Returns the reverse certificate.
     */
    public X509Certificate getReverse()
    {
        return reverse;
    }

    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (!(o instanceof X509CertificatePair))
        {
            return false;
        }
        X509CertificatePair pair = (X509CertificatePair)o;
        boolean equalReverse = true;
        boolean equalForward = true;
        if (forward != null)
        {
            equalForward = this.forward.equals(pair.forward);
        }
        else
        {
            if (pair.forward != null)
            {
                equalForward = false;
            }
        }
        if (reverse != null)
        {
            equalReverse = this.reverse.equals(pair.reverse);
        }
        else
        {
            if (pair.reverse != null)
            {
                equalReverse = false;
            }
        }
        return equalForward && equalReverse;
    }

    public int hashCode()
    {
        int hash = -1;
        if (forward != null)
        {
            hash ^= forward.hashCode();
        }
        if (reverse != null)
        {
            hash *= 17;
            hash ^= reverse.hashCode();
        }
        return hash;
    }
}
