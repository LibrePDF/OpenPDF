package old.org.bouncycastle.x509;

import java.security.cert.CertificateEncodingException;

class ExtCertificateEncodingException
    extends CertificateEncodingException
{
    Throwable cause;

    ExtCertificateEncodingException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
