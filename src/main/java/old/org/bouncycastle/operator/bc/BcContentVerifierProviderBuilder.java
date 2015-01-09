package old.org.bouncycastle.operator.bc;

import java.io.IOException;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.crypto.Signer;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.operator.ContentVerifier;
import old.org.bouncycastle.operator.ContentVerifierProvider;
import old.org.bouncycastle.operator.OperatorCreationException;

public abstract class BcContentVerifierProviderBuilder
{
    public BcContentVerifierProviderBuilder()
    {
    }

    public ContentVerifierProvider build(final X509CertificateHolder certHolder)
        throws OperatorCreationException
    {
        return new ContentVerifierProvider()
        {
            public boolean hasAssociatedCertificate()
            {
                return true;
            }

            public X509CertificateHolder getAssociatedCertificate()
            {
                return certHolder;
            }

            public ContentVerifier get(AlgorithmIdentifier algorithm)
                throws OperatorCreationException
            {
                try
                {
                    AsymmetricKeyParameter publicKey = extractKeyParameters(certHolder.getSubjectPublicKeyInfo());
                    BcSignerOutputStream stream = createSignatureStream(algorithm, publicKey);

                    return new SigVerifier(algorithm, stream);
                }
                catch (IOException e)
                {
                    throw new OperatorCreationException("exception on setup: " + e, e);
                }
            }
        };
    }

    public ContentVerifierProvider build(final AsymmetricKeyParameter publicKey)
        throws OperatorCreationException
    {
        return new ContentVerifierProvider()
        {
            public boolean hasAssociatedCertificate()
            {
                return false;
            }

            public X509CertificateHolder getAssociatedCertificate()
            {
                return null;
            }

            public ContentVerifier get(AlgorithmIdentifier algorithm)
                throws OperatorCreationException
            {
                BcSignerOutputStream stream = createSignatureStream(algorithm, publicKey);

                return new SigVerifier(algorithm, stream);
            }
        };
    }

    private BcSignerOutputStream createSignatureStream(AlgorithmIdentifier algorithm, AsymmetricKeyParameter publicKey)
        throws OperatorCreationException
    {
        Signer sig = createSigner(algorithm);

        sig.init(false, publicKey);

        return new BcSignerOutputStream(sig);
    }

    /**
     * Extract an AsymmetricKeyParameter from the passed in SubjectPublicKeyInfo structure.
     *
     * @param publicKeyInfo a publicKeyInfo structure describing the public key required.
     * @return an AsymmetricKeyParameter object containing the appropriate public key.
     * @throws IOException if the publicKeyInfo data cannot be parsed,
     */
    protected abstract AsymmetricKeyParameter extractKeyParameters(SubjectPublicKeyInfo publicKeyInfo)
        throws IOException;

    /**
     * Create the correct signer for the algorithm identifier sigAlgId.
     *
     * @param sigAlgId the algorithm details for the signature we want to verify.
     * @return a Signer object.
     * @throws OperatorCreationException if the Signer cannot be constructed.
     */
    protected abstract Signer createSigner(AlgorithmIdentifier sigAlgId)
        throws OperatorCreationException;

    private class SigVerifier
        implements ContentVerifier
    {
        private BcSignerOutputStream stream;
        private AlgorithmIdentifier algorithm;

        SigVerifier(AlgorithmIdentifier algorithm, BcSignerOutputStream stream)
        {
            this.algorithm = algorithm;
            this.stream = stream;
        }

        public AlgorithmIdentifier getAlgorithmIdentifier()
        {
            return algorithm;
        }

        public OutputStream getOutputStream()
        {
            if (stream == null)
            {
                throw new IllegalStateException("verifier not initialised");
            }

            return stream;
        }

        public boolean verify(byte[] expected)
        {
            return stream.verify(expected);
        }
    }
}