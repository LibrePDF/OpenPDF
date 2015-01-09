package old.org.bouncycastle.crypto.tls;

import old.org.bouncycastle.crypto.DSA;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import old.org.bouncycastle.crypto.signers.ECDSASigner;

class TlsECDSASigner extends TlsDSASigner
{
    public boolean isValidPublicKey(AsymmetricKeyParameter publicKey)
    {
        return publicKey instanceof ECPublicKeyParameters;
    }

    protected DSA createDSAImpl()
    {
        return new ECDSASigner();
    }
}
