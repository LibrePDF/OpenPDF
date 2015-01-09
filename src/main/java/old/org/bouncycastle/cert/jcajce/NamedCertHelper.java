package old.org.bouncycastle.cert.jcajce;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

class NamedCertHelper
    extends CertHelper
{
    private final String providerName;

    NamedCertHelper(String providerName)
    {
        this.providerName = providerName;
    }

    protected CertificateFactory createCertificateFactory(String type)
        throws CertificateException, NoSuchProviderException
    {
        return CertificateFactory.getInstance(type, providerName);
    }
}