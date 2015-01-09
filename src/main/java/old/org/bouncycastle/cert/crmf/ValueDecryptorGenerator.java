package old.org.bouncycastle.cert.crmf;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.operator.InputDecryptor;

public interface ValueDecryptorGenerator
{
    InputDecryptor getValueDecryptor(AlgorithmIdentifier keyAlg, AlgorithmIdentifier symmAlg, byte[] encKey)
        throws CRMFException;
}
