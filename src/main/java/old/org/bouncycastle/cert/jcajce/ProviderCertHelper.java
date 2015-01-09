package old.org.bouncycastle.cert.jcajce;

import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

class ProviderCertHelper
    extends CertHelper
{
    private final Provider provider;

    ProviderCertHelper(Provider provider)
    {
        this.provider = provider;
    }

    protected CertificateFactory createCertificateFactory(String type)
        throws CertificateException
    {
        return CertificateFactory.getInstance(type, provider);
    }
}