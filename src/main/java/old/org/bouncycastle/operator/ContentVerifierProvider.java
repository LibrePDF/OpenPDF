package old.org.bouncycastle.operator;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cert.X509CertificateHolder;

/**
 * General interface for providers of ContentVerifier objects.
 */
public interface ContentVerifierProvider
{
    /**
     * Return whether or not this verifier has a certificate associated with it.
     *
     * @return true if there is an associated certificate, false otherwise.
     */
    boolean hasAssociatedCertificate();

    /**
     * Return the associated certificate if there is one.
     *
     * @return a holder containing the associated certificate if there is one, null if there is not.
     */
    X509CertificateHolder getAssociatedCertificate();

    /**
     * Return a ContentVerifier that matches the passed in algorithm identifier,
     *
     * @param verifierAlgorithmIdentifier the algorithm and parameters required.
     * @return a matching ContentVerifier
     * @throws OperatorCreationException if the required ContentVerifier cannot be created.
     */
    ContentVerifier get(AlgorithmIdentifier verifierAlgorithmIdentifier)
        throws OperatorCreationException;
}
