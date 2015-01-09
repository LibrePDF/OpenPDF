package old.org.bouncycastle.cms.jcajce;

import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.RecipientOperator;
import old.org.bouncycastle.operator.InputDecryptor;

public class JceKeyAgreeEnvelopedRecipient
    extends JceKeyAgreeRecipient
{
    public JceKeyAgreeEnvelopedRecipient(PrivateKey recipientKey)
    {
        super(recipientKey);
    }

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier keyEncryptionAlgorithm, final AlgorithmIdentifier contentEncryptionAlgorithm, SubjectPublicKeyInfo senderPublicKey, ASN1OctetString userKeyingMaterial, byte[] encryptedContentKey)
        throws CMSException
    {
        Key secretKey = extractSecretKey(keyEncryptionAlgorithm, contentEncryptionAlgorithm, senderPublicKey, userKeyingMaterial, encryptedContentKey);

        final Cipher dataCipher = contentHelper.createContentCipher(secretKey, contentEncryptionAlgorithm);

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
