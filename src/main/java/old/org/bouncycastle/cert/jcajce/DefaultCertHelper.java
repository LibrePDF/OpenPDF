package old.org.bouncycastle.cert.jcajce;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

class DefaultCertHelper
    extends CertHelper
{
    protected CertificateFactory createCertificateFactory(String type)
        throws CertificateException
    {
        return CertificateFactory.getInstance(type);
    }
}
