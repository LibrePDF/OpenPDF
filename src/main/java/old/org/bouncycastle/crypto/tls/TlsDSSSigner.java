package old.org.bouncycastle.crypto.tls;

import old.org.bouncycastle.crypto.DSA;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import old.org.bouncycastle.crypto.signers.DSASigner;

class TlsDSSSigner extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof DSAPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new DSASigner();
    }
}
