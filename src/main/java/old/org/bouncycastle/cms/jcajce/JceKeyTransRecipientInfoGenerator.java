package old.org.bouncycastle.cms.jcajce;

import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import old.org.bouncycastle.cms.KeyTransRecipientInfoGenerator;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.operator.jcajce.JceAsymmetricKeyWrapper;

public class JceKeyTransRecipientInfoGenerator
    extends KeyTransRecipientInfoGenerator
{
    public JceKeyTransRecipientInfoGenerator(X509Certificate recipientCert)
        throws CertificateEncodingException
    {
        super(new JcaX509CertificateHolder(recipientCert).getIssuerAndSerialNumber(), new JceAsymmetricKeyWrapper(recipientCert.getPublicKey()));
    }

    public JceKeyTransRecipientInfoGenerator(byte[] subjectKeyIdentifier, PublicKey publicKey)
    {
        super(subjectKeyIdentifier, new JceAsymmetricKeyWrapper(publicKey));
    }

    public JceKeyTransRecipientInfoGenerator setProvider(String providerName)
        throws OperatorCreationException
    {
        ((JceAsymmetricKeyWrapper)this.wrapper).setProvider(providerName);

        return this;
    }

    public JceKeyTransRecipientInfoGenerator setProvider(Provider provider)
        throws OperatorCreationException
    {
        ((JceAsymmetricKeyWrapper)this.wrapper).setProvider(provider);

        return this;
    }
}