package old.org.bouncycastle.x509;

import old.org.bouncycastle.util.Selector;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;

/**
 * This class is a Selector implementation for X.509 certificates.
 * 
 * @see old.org.bouncycastle.util.Selector
 * @see old.org.bouncycastle.x509.X509Store
 * @see old.org.bouncycastle.jce.provider.X509StoreCertCollection
 */
public class X509CertStoreSelector
    extends X509CertSelector
    implements Selector
{
    public boolean match(Object obj)
    {
        if (!(obj instanceof X509Certificate))
        {
            return false;
        }

        X509Certificate other = (X509Certificate)obj;

        return super.match(other);
    }

    public boolean match(Certificate cert)
    {
        return match((Object)cert);
    }

    public Object clone()
    {
        X509CertStoreSelector selector = (X509CertStoreSelector)super.clone();

        return selector;
    }

    /**
     * Returns an instance of this from a <code>X509CertSelector</code>.
     *
     * @param selector A <code>X509CertSelector</code> instance.
     * @return An instance of an <code>X509CertStoreSelector</code>.
     * @exception IllegalArgumentException if selector is null or creation fails.
     */
    public static X509CertStoreSelector getInstance(X509CertSelector selector)
    {
        if (selector == null)
        {
            throw new IllegalArgumentException("cannot create from null selector");
        }
        X509CertStoreSelector cs = new X509CertStoreSelector();
        cs.setAuthorityKeyIdentifier(selector.getAuthorityKeyIdentifier());
        cs.setBasicConstraints(selector.getBasicConstraints());
        cs.setCertificate(selector.getCertificate());
        cs.setCertificateValid(selector.getCertificateValid());
        cs.setMatchAllSubjectAltNames(selector.getMatchAllSubjectAltNames());
        try
        {
            cs.setPathToNames(selector.getPathToNames());
            cs.setExtendedKeyUsage(selector.getExtendedKeyUsage());
            cs.setNameConstraints(selector.getNameConstraints());
            cs.setPolicy(selector.getPolicy());
            cs.setSubjectPublicKeyAlgID(selector.getSubjectPublicKeyAlgID());
            cs.setSubjectAlternativeNames(selector.getSubjectAlternativeNames());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("error in passed in selector: " + e);
        }
        cs.setIssuer(selector.getIssuer());
        cs.setKeyUsage(selector.getKeyUsage());
        cs.setPrivateKeyValid(selector.getPrivateKeyValid());
        cs.setSerialNumber(selector.getSerialNumber());
        cs.setSubject(selector.getSubject());
        cs.setSubjectKeyIdentifier(selector.getSubjectKeyIdentifier());
        cs.setSubjectPublicKey(selector.getSubjectPublicKey());
        return cs;
    }

}
