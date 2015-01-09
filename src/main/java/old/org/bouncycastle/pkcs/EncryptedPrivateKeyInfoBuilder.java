package old.org.bouncycastle.pkcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import old.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import old.org.bouncycastle.operator.OutputEncryptor;

/**
 * A class for creating EncryptedPrivateKeyInfo structures.
 * <pre>
 * EncryptedPrivateKeyInfo ::= SEQUENCE {
 *      encryptionAlgorithm AlgorithmIdentifier {{KeyEncryptionAlgorithms}},
 *      encryptedData EncryptedData
 * }
 *
 * EncryptedData ::= OCTET STRING
 *
 * KeyEncryptionAlgorithms ALGORITHM-IDENTIFIER ::= {
 *          ... -- For local profiles
 * }
 * </pre>
 */
public class EncryptedPrivateKeyInfoBuilder
{
    private PrivateKeyInfo privateKeyInfo;

    public EncryptedPrivateKeyInfoBuilder(PrivateKeyInfo privateKeyInfo)
    {
        this.privateKeyInfo = privateKeyInfo;
    }

    public EncryptedPrivateKeyInfoHolder build(
        OutputEncryptor encryptor)
    {
        try
        {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            OutputStream cOut = encryptor.getOutputStream(bOut);

            cOut.write(privateKeyInfo.getEncoded());

            cOut.close();

            return new EncryptedPrivateKeyInfoHolder(new EncryptedPrivateKeyInfo(encryptor.getAlgorithmIdentifier(), bOut.toByteArray()));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("cannot encode privateKeyInfo");
        }
    }
}
