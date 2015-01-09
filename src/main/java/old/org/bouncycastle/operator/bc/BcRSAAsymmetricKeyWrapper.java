package old.org.bouncycastle.operator.bc;

import java.io.IOException;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.crypto.AsymmetricBlockCipher;
import old.org.bouncycastle.crypto.encodings.PKCS1Encoding;
import old.org.bouncycastle.crypto.engines.RSAEngine;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.util.PublicKeyFactory;

public class BcRSAAsymmetricKeyWrapper
    extends BcAsymmetricKeyWrapper
{
    public BcRSAAsymmetricKeyWrapper(AlgorithmIdentifier encAlgId, AsymmetricKeyParameter publicKey)
    {
        super(encAlgId, publicKey);
    }

    public BcRSAAsymmetricKeyWrapper(AlgorithmIdentifier encAlgId, SubjectPublicKeyInfo publicKeyInfo)
        throws IOException
    {
        super(encAlgId, PublicKeyFactory.createKey(publicKeyInfo));
    }

    protected AsymmetricBlockCipher createAsymmetricWrapper(ASN1ObjectIdentifier algorithm)
    {
        return new PKCS1Encoding(new RSAEngine());
    }
}
