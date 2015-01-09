package old.org.bouncycastle.cms.bc;

import java.security.cert.CertificateException;

import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.cms.SignerInformationVerifier;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;

public class BcRSASignerInfoVerifierBuilder
{
    private BcRSAContentVerifierProviderBuilder contentVerifierProviderBuilder;
    private DigestCalculatorProvider digestCalculatorProvider;

    public BcRSASignerInfoVerifierBuilder(DigestAlgorithmIdentifierFinder digestAlgorithmFinder, DigestCalculatorProvider digestCalculatorProvider)
    {
        this.contentVerifierProviderBuilder = new BcRSAContentVerifierProviderBuilder(digestAlgorithmFinder);
        this.digestCalculatorProvider = digestCalculatorProvider;
    }

    public SignerInformationVerifier build(X509CertificateHolder certHolder)
        throws OperatorCreationException, CertificateException
    {
        return new SignerInformationVerifier(contentVerifierProviderBuilder.build(certHolder), digestCalculatorProvider);
    }

    public SignerInformationVerifier build(AsymmetricKeyParameter pubKey)
        throws OperatorCreationException
    {
        return new SignerInformationVerifier(contentVerifierProviderBuilder.build(pubKey), digestCalculatorProvider);
    }
}