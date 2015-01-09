package old.org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsCipherFactory
{
    /**
     * See enumeration classes EncryptionAlgorithm and DigestAlgorithm for appropriate argument values
     */
    TlsCipher createCipher(TlsClientContext context, int encryptionAlgorithm, int digestAlgorithm) throws IOException;
}
