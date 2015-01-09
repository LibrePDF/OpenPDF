package old.org.bouncycastle.operator;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface DigestAlgorithmIdentifierFinder
{
    /**
     * Find the digest algorithm identifier that matches with
     * the passed in signature algorithm identifier.
     *
     * @param sigAlgId the signature algorithm of interest.
     * @return an algorithm identifier for the corresponding digest.
     */
    AlgorithmIdentifier find(AlgorithmIdentifier sigAlgId);
}