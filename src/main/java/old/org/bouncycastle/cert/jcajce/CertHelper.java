package old.org.bouncycastle.cert.jcajce;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

abstract class CertHelper
{
    public CertificateFactory getCertificateFactory(String type)
        throws NoSuchProviderException, CertificateException
    {
        return createCertificateFactory(type);
    }

    protected abstract CertificateFactory createCertificateFactory(String type)
        throws CertificateException, NoSuchProviderException;
}
