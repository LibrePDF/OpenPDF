package old.org.bouncycastle.cert.cmp;

import java.math.BigInteger;

import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.cmp.RevDetails;
import old.org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public class RevocationDetailsBuilder
{
    private CertTemplateBuilder templateBuilder = new CertTemplateBuilder();
    
    public RevocationDetailsBuilder setPublicKey(SubjectPublicKeyInfo publicKey)
    {
        if (publicKey != null)
        {
            templateBuilder.setPublicKey(publicKey);
        }

        return this;
    }

    public RevocationDetailsBuilder setIssuer(X500Name issuer)
    {
        if (issuer != null)
        {
            templateBuilder.setIssuer(issuer);
        }

        return this;
    }

    public RevocationDetailsBuilder setSerialNumber(BigInteger serialNumber)
    {
        if (serialNumber != null)
        {
            templateBuilder.setSerialNumber(new DERInteger(serialNumber));
        }

        return this;
    }

    public RevocationDetailsBuilder setSubject(X500Name subject)
    {
        if (subject != null)
        {
            templateBuilder.setSubject(subject);
        }

        return this;
    }

    public RevocationDetails build()
    {
        return new RevocationDetails(new RevDetails(templateBuilder.build()));
    }
}
