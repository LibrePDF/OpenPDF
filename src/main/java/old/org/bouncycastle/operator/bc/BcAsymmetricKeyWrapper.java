package old.org.bouncycastle.operator.bc;

import java.security.SecureRandom;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.crypto.AsymmetricBlockCipher;
import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.InvalidCipherTextException;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import old.org.bouncycastle.crypto.params.ParametersWithRandom;
import old.org.bouncycastle.operator.AsymmetricKeyWrapper;
import old.org.bouncycastle.operator.GenericKey;
import old.org.bouncycastle.operator.OperatorException;

public abstract class BcAsymmetricKeyWrapper
    extends AsymmetricKeyWrapper
{
    private AsymmetricKeyParameter publicKey;
    private SecureRandom random;

    public BcAsymmetricKeyWrapper(AlgorithmIdentifier encAlgId, AsymmetricKeyParameter publicKey)
    {
        super(encAlgId);

        this.publicKey = publicKey;
    }

    public BcAsymmetricKeyWrapper setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public byte[] generateWrappedKey(GenericKey encryptionKey)
        throws OperatorException
    {
        AsymmetricBlockCipher keyEncryptionCipher = createAsymmetricWrapper(getAlgorithmIdentifier().getAlgorithm());
        
        CipherParameters params = publicKey;
        if (random != null)
        {
            params = new ParametersWithRandom(params, random);
        }

        try
        {
            byte[] keyEnc = OperatorUtils.getKeyBytes(encryptionKey);
            keyEncryptionCipher.init(true, publicKey);
            return keyEncryptionCipher.processBlock(keyEnc, 0, keyEnc.length);
        }
        catch (InvalidCipherTextException e)
        {
            throw new OperatorException("unable to encrypt contents key", e);
        }
    }

    protected abstract AsymmetricBlockCipher createAsymmetricWrapper(ASN1ObjectIdentifier algorithm);
}
