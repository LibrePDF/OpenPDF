package old.org.bouncycastle.operator.bc;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.Signer;
import old.org.bouncycastle.crypto.signers.RSADigestSigner;
import old.org.bouncycastle.operator.OperatorCreationException;

public class BcRSAContentSignerBuilder
    extends BcContentSignerBuilder
{
    public BcRSAContentSignerBuilder(AlgorithmIdentifier sigAlgId, AlgorithmIdentifier digAlgId)
    {
        super(sigAlgId, digAlgId);
    }

    protected Signer createSigner(AlgorithmIdentifier sigAlgId, AlgorithmIdentifier digAlgId)
        throws OperatorCreationException
    {
        Digest dig = BcUtil.createDigest(digAlgId);

        return new RSADigestSigner(dig);
    }
}
