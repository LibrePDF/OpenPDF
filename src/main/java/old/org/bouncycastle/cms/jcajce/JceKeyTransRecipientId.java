package old.org.bouncycastle.cms.jcajce;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.cms.KeyTransRecipientId;

public class JceKeyTransRecipientId
    extends KeyTransRecipientId
{
    public JceKeyTransRecipientId(X509Certificate certificate)
    {
        this(certificate.getIssuerX500Principal(), certificate.getSerialNumber());
    }

    public JceKeyTransRecipientId(X500Principal issuer, BigInteger serialNumber)
    {
        super(X500Name.getInstance(issuer.getEncoded()), serialNumber);
    }
}
