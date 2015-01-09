package old.org.bouncycastle.cert.jcajce;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

import old.org.bouncycastle.cert.X509CRLHolder;

/**
 * Class for converting an X509CRLHolder into a corresponding X509CRL object tied to a
 * particular JCA provider.
 */
public class JcaX509CRLConverter
{
    private CertHelper helper = new DefaultCertHelper();

    /**
     * Base constructor, configure with the default provider.
     */
    public JcaX509CRLConverter()
    {
        this.helper = new DefaultCertHelper();
    }

    /**
     * Set the provider to use from a Provider object.
     *
     * @param provider the provider to use.
     * @return the converter instance.
     */
    public JcaX509CRLConverter setProvider(Provider provider)
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
    public JcaX509CRLConverter setProvider(String providerName)
    {
        this.helper = new NamedCertHelper(providerName);

        return this;
    }

    /**
     * Use the configured converter to produce a X509CRL object from a X509CRLHolder object.
     *
     * @param crlHolder  the holder to be converted
     * @return a X509CRL object
     * @throws CRLException if the conversion is unable to be made.
     */
    public X509CRL getCRL(X509CRLHolder crlHolder)
        throws CRLException
    {
        try
        {
            CertificateFactory cFact = helper.getCertificateFactory("X.509");

            return (X509CRL)cFact.generateCRL(new ByteArrayInputStream(crlHolder.getEncoded()));
        }
        catch (IOException e)
        {
            throw new ExCRLException("exception parsing certificate: " + e.getMessage(), e);
        }
        catch (NoSuchProviderException e)
        {
            throw new ExCRLException("cannot find required provider:" + e.getMessage(), e);
        }
        catch (CertificateException e)
        {
            throw new ExCRLException("cannot create factory: " + e.getMessage(), e);
        }
    }

    private class ExCRLException
        extends CRLException
    {
        private Throwable cause;

        public ExCRLException(String msg, Throwable cause)
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
