package old.org.bouncycastle.cert.jcajce;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.cert.X509CertificateHolder;

/**
 * JCA helper class for converting an X509Certificate into a X509CertificateHolder object.
 */
public class JcaX509CertificateHolder
    extends X509CertificateHolder
{
    /**
     * Base constructor.
     *
     * @param cert certificate to be used a the source for the holder creation.
     * @throws CertificateEncodingException if there is a problem extracting the certificate information.
     */
    public JcaX509CertificateHolder(X509Certificate cert)
        throws CertificateEncodingException
    {
        super(X509CertificateStructure.getInstance(cert.getEncoded()));
    }
}
