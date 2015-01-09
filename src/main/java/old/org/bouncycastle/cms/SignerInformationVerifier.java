package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;

public class SignerInformationVerifier
{
    private ContentVerifierProvider verifierProvider;
    private DigestCalculatorProvider digestProvider;

    public SignerInformationVerifier(ContentVerifierProvider verifierProvider, DigestCalculatorProvider digestProvider)
    {
        this.verifierProvider = verifierProvider;
        this.digestProvider = digestProvider;
    }

    public boolean hasAssociatedCertificate()
    {
        return verifierProvider.hasAssociatedCertificate();
    }

    public X509CertificateHolder getAssociatedCertificate()
    {
        return verifierProvider.getAssociatedCertificate();
    }

    public ContentVerifier getContentVerifier(AlgorithmIdentifier algorithmIdentifier)
        throws OperatorCreationException
    {
        return verifierProvider.get(algorithmIdentifier);
    }

    public DigestCalculator getDigestCalculator(AlgorithmIdentifier algorithmIdentifier)
        throws OperatorCreationException
    {
        return digestProvider.get(algorithmIdentifier);
    }
}
