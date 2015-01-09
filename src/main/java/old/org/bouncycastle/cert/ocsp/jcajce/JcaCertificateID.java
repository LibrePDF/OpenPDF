package old.org.bouncycastle.cert.ocsp.jcajce;

import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import old.org.bouncycastle.cert.ocsp.CertificateID;
import old.org.bouncycastle.cert.ocsp.OCSPException;
import old.org.bouncycastle.operator.DigestCalculator;

public class JcaCertificateID
    extends CertificateID
{
    public JcaCertificateID(DigestCalculator digestCalculator, X509Certificate issuerCert, BigInteger number)
        throws OCSPException, CertificateEncodingException
    {
        super(digestCalculator, new JcaX509CertificateHolder(issuerCert), number);
    }
}
