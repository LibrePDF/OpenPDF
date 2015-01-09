package old.org.bouncycastle.crypto.tls;

import java.io.IOException;

import old.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface TlsAgreementCredentials extends TlsCredentials
{
    byte[] generateAgreement(AsymmetricKeyParameter serverPublicKey) throws IOException;
}
