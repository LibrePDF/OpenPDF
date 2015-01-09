package old.org.bouncycastle.cert.ocsp.jcajce;

import java.security.PublicKey;

import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import old.org.bouncycastle.cert.ocsp.OCSPException;
import old.org.bouncycastle.operator.DigestCalculator;

public class JcaBasicOCSPRespBuilder
    extends BasicOCSPRespBuilder
{
    public JcaBasicOCSPRespBuilder(PublicKey key, DigestCalculator digCalc)
        throws OCSPException
    {
        super(SubjectPublicKeyInfo.getInstance(key.getEncoded()), digCalc);
    }
}
