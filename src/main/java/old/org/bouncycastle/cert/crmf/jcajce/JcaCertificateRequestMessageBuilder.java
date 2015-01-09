package old.org.bouncycastle.cert.crmf.jcajce;

import java.math.BigInteger;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.cert.crmf.CertificateRequestMessageBuilder;

public class JcaCertificateRequestMessageBuilder
    extends CertificateRequestMessageBuilder
{
    public JcaCertificateRequestMessageBuilder(BigInteger certReqId)
    {
        super(certReqId);
    }

    public JcaCertificateRequestMessageBuilder setIssuer(X500Principal issuer)
    {
        if (issuer != null)
        {
            setIssuer(X500Name.getInstance(issuer.getEncoded()));
        }

        return this;
    }

    public JcaCertificateRequestMessageBuilder setSubject(X500Principal subject)
    {
        if (subject != null)
        {
            setSubject(X500Name.getInstance(subject.getEncoded()));
        }

        return this;
    }

    public JcaCertificateRequestMessageBuilder setAuthInfoSender(X500Principal sender)
    {
        if (sender != null)
        {
            setAuthInfoSender(new GeneralName(X500Name.getInstance(sender.getEncoded())));
        }

        return this;
    }

    public JcaCertificateRequestMessageBuilder setPublicKey(PublicKey publicKey)
    {
        setPublicKey(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));

        return this;
    }
}
