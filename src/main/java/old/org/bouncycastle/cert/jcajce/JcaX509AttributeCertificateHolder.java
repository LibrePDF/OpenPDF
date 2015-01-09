package old.org.bouncycastle.cert.jcajce;

import java.io.IOException;

import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.cert.X509AttributeCertificateHolder;
import old.org.bouncycastle.x509.X509AttributeCertificate;

/**
 * JCA helper class for converting an old style X509AttributeCertificate into a X509AttributeCertificateHolder object.
 */
public class JcaX509AttributeCertificateHolder
    extends X509AttributeCertificateHolder
{
    /**
     * Base constructor.
     *
     * @param cert AttributeCertificate to be used a the source for the holder creation.
     * @throws IOException if there is a problem extracting the attribute certificate information.
     */
    public JcaX509AttributeCertificateHolder(X509AttributeCertificate cert)
        throws IOException
    {
        super(AttributeCertificate.getInstance(cert.getEncoded()));
    }
}
