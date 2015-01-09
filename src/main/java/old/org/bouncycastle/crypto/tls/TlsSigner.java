package old.org.bouncycastle.crypto.tls;

import java.security.SecureRandom;

import old.org.bouncycastle.crypto.CryptoException;
import old.org.bouncycastle.crypto.Signer;
import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

interface TlsSigner
{
    byte[] calculateRawSignature(SecureRandom random, AsymmetricKeyParameter privateKey, byte[] md5andsha1)
        throws CryptoException;

    Signer createVerifyer(AsymmetricKeyParameter publicKey);

    boolean isValidPublicKey(AsymmetricKeyParameter publicKey);
}
