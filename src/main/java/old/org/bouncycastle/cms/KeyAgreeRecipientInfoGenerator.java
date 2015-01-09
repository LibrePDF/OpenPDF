package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.cms.KeyAgreeRecipientInfo;
import old.org.bouncycastle.asn1.cms.OriginatorIdentifierOrKey;
import old.org.bouncycastle.asn1.cms.OriginatorPublicKey;
import old.org.bouncycastle.asn1.cms.RecipientInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.operator.GenericKey;

public abstract class KeyAgreeRecipientInfoGenerator
    implements RecipientInfoGenerator
{
    private ASN1ObjectIdentifier keyAgreementOID;
    private ASN1ObjectIdentifier keyEncryptionOID;
    private SubjectPublicKeyInfo originatorKeyInfo;

    protected KeyAgreeRecipientInfoGenerator(ASN1ObjectIdentifier keyAgreementOID, SubjectPublicKeyInfo originatorKeyInfo, ASN1ObjectIdentifier keyEncryptionOID)
    {
        this.originatorKeyInfo = originatorKeyInfo;
        this.keyAgreementOID = keyAgreementOID;
        this.keyEncryptionOID = keyEncryptionOID;
    }

    public RecipientInfo generate(GenericKey contentEncryptionKey)
        throws CMSException
    {
        OriginatorIdentifierOrKey originator = new OriginatorIdentifierOrKey(
                createOriginatorPublicKey(originatorKeyInfo));

        ASN1EncodableVector params = new ASN1EncodableVector();
        params.add(keyEncryptionOID);
        params.add(DERNull.INSTANCE);
        AlgorithmIdentifier keyEncAlg = new AlgorithmIdentifier(keyEncryptionOID, DERNull.INSTANCE);
        AlgorithmIdentifier keyAgreeAlg = new AlgorithmIdentifier(keyAgreementOID, keyEncAlg);

        ASN1Sequence recipients = generateRecipientEncryptedKeys(keyAgreeAlg, keyEncAlg, contentEncryptionKey);
        ASN1Encodable userKeyingMaterial = getUserKeyingMaterial(keyAgreeAlg);

        if (userKeyingMaterial != null)
        {
            return new RecipientInfo(new KeyAgreeRecipientInfo(originator, new DEROctetString(userKeyingMaterial),
                keyAgreeAlg, recipients));
        }
        else
        {
            return new RecipientInfo(new KeyAgreeRecipientInfo(originator, null,
                keyAgreeAlg, recipients));
        }
    }

    protected OriginatorPublicKey createOriginatorPublicKey(SubjectPublicKeyInfo originatorKeyInfo)
    {
        return new OriginatorPublicKey(
            new AlgorithmIdentifier(originatorKeyInfo.getAlgorithmId().getAlgorithm(), DERNull.INSTANCE),
            originatorKeyInfo.getPublicKeyData().getBytes());
    }

    protected abstract ASN1Sequence generateRecipientEncryptedKeys(AlgorithmIdentifier keyAgreeAlgorithm, AlgorithmIdentifier keyEncAlgorithm, GenericKey contentEncryptionKey)
        throws CMSException;

    protected abstract ASN1Encodable getUserKeyingMaterial(AlgorithmIdentifier keyAgreeAlgorithm)
        throws CMSException;

}