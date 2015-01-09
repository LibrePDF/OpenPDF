package old.org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsSignerCredentials extends TlsCredentials
{
    byte[] generateCertificateSignature(byte[] md5andsha1) throws IOException;
}
