package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public interface KeyAgreeRecipient
    extends Recipient
{
    RecipientOperator getRecipientOperator(AlgorithmIdentifier keyEncAlg, AlgorithmIdentifier contentEncryptionAlgorithm, SubjectPublicKeyInfo senderPublicKey, ASN1OctetString userKeyingMaterial, byte[] encryptedContentKey)
        throws CMSException;

    AlgorithmIdentifier getPrivateKeyAlgorithmIdentifier();
}
