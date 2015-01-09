package old.org.bouncycastle.cms.jcajce;

import java.io.InputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.RecipientOperator;
import old.org.bouncycastle.operator.InputDecryptor;

public class JcePasswordEnvelopedRecipient
    extends JcePasswordRecipient
{
    public JcePasswordEnvelopedRecipient(char[] password)
    {
        super(password);
    }

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier keyEncryptionAlgorithm, final AlgorithmIdentifier contentEncryptionAlgorithm, byte[] derivedKey, byte[] encryptedContentEncryptionKey)
        throws CMSException
    {
        Key secretKey = extractSecretKey(keyEncryptionAlgorithm, contentEncryptionAlgorithm, derivedKey, encryptedContentEncryptionKey);

        final Cipher dataCipher = helper.createContentCipher(secretKey, contentEncryptionAlgorithm);

        return new RecipientOperator(new InputDecryptor()
        {
            public AlgorithmIdentifier getAlgorithmIdentifier()
            {
                return contentEncryptionAlgorithm;
            }

            public InputStream getInputStream(InputStream dataOut)
            {
                return new CipherInputStream(dataOut, dataCipher);
            }
        });
    }
}
