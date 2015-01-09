package old.org.bouncycastle.cert.crmf.jcajce;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.asn1.crmf.EncryptedValue;
import old.org.bouncycastle.cert.crmf.CRMFException;
import old.org.bouncycastle.cert.crmf.EncryptedValueBuilder;
import old.org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import old.org.bouncycastle.operator.KeyWrapper;
import old.org.bouncycastle.operator.OutputEncryptor;

public class JcaEncryptedValueBuilder
    extends EncryptedValueBuilder
{
    public JcaEncryptedValueBuilder(KeyWrapper wrapper, OutputEncryptor encryptor)
    {
        super(wrapper, encryptor);
    }

    public EncryptedValue build(X509Certificate certificate)
        throws CertificateEncodingException, CRMFException
    {
        return build(new JcaX509CertificateHolder(certificate));
    }
}
