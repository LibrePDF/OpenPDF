package old.org.bouncycastle.cert.jcajce;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.cert.X509CertificateHolder;

/**
 * Converter for producing X509Certificate objects tied to a specific provider from X509CertificateHolder objects.
 */
public class JcaX509CertificateConverter
{
    private CertHelper helper = new DefaultCertHelper();

    /**
     * Base constructor, configure with the default provider.
     */
    public JcaX509CertificateConverter()
    {
        this.helper = new DefaultCertHelper();
    }

    /**
     * Set the provider to use from a Provider object.
     *
     * @param provider the provider to use.
     * @return the converter instance.
     */
    public JcaX509CertificateConverter setProvider(Provider provider)
    {
        this.helper = new ProviderCertHelper(provider);

        return this;
    }

    /**
     * Set the provider to use by name.
     *
     * @param providerName name of the provider to use.
     * @return the converter instance.
     */
    public JcaX509CertificateConverter setProvider(String providerName)
    {
        this.helper = new NamedCertHelper(providerName);

        return this;
    }

    /**
     * Use the configured converter to produce a X509Certificate object from a X509CertificateHolder object.
     *
     * @param certHolder  the holder to be converted
     * @return a X509Certificate object
     * @throws CertificateException if the conversion is unable to be made.
     */
    public X509Certificate getCertificate(X509CertificateHolder certHolder)
        throws CertificateException
    {
        try
        {
            CertificateFactory cFact = helper.getCertificateFactory("X.509");

            return (X509Certificate)cFact.generateCertificate(new ByteArrayInputStream(certHolder.getEncoded()));
        }
        catch (IOException e)
        {
            throw new ExCertificateParsingException("exception parsing certificate: " + e.getMessage(), e);
        }
        catch (NoSuchProviderException e)
        {
            throw new ExCertificateException("cannot find required provider:" + e.getMessage(), e);
        }
    }

    private class ExCertificateParsingException
        extends CertificateParsingException
    {
        private Throwable cause;

        public ExCertificateParsingException(String msg, Throwable cause)
        {
            super(msg);

            this.cause = cause;
        }

        public Throwable getCause()
        {
            return cause;
        }
    }
    
    private class ExCertificateException
        extends CertificateException
    {
        private Throwable cause;

        public ExCertificateException(String msg, Throwable cause)
        {
            super(msg);

            this.cause = cause;
        }

        public Throwable getCause()
        {
            return cause;
        }
    }
}