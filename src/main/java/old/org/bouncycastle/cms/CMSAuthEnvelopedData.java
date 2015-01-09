package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;

import javax.crypto.SecretKey;

import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.cms.AuthEnvelopedData;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.cms.EncryptedContentInfo;
import old.org.bouncycastle.asn1.cms.OriginatorInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * containing class for an CMS AuthEnveloped Data object
 */
class CMSAuthEnvelopedData
{
    RecipientInformationStore recipientInfoStore;
    ContentInfo contentInfo;

    private OriginatorInfo      originator;
    private AlgorithmIdentifier authEncAlg;
    private ASN1Set             authAttrs;
    private byte[]              mac;
    private ASN1Set             unauthAttrs;

    public CMSAuthEnvelopedData(byte[] authEnvData) throws CMSException
    {
        this(CMSUtils.readContentInfo(authEnvData));
    }

    public CMSAuthEnvelopedData(InputStream authEnvData) throws CMSException
    {
        this(CMSUtils.readContentInfo(authEnvData));
    }

    public CMSAuthEnvelopedData(ContentInfo contentInfo) throws CMSException
    {
        this.contentInfo = contentInfo;

        AuthEnvelopedData authEnvData = AuthEnvelopedData.getInstance(contentInfo.getContent());

        this.originator = authEnvData.getOriginatorInfo();

        //
        // read the recipients
        //
        ASN1Set recipientInfos = authEnvData.getRecipientInfos();

        //
        // read the auth-encrypted content info
        //
        EncryptedContentInfo authEncInfo = authEnvData.getAuthEncryptedContentInfo();
        this.authEncAlg = authEncInfo.getContentEncryptionAlgorithm();
//        final CMSProcessable processable = new CMSProcessableByteArray(
//            authEncInfo.getEncryptedContent().getOctets());
        CMSSecureReadable secureReadable = new CMSSecureReadable()
        {
            public AlgorithmIdentifier getAlgorithm()
            {
                return CMSAuthEnvelopedData.this.authEncAlg;
            }

            public Object getCryptoObject()
            {
                return null;
            }

            public CMSReadable getReadable(SecretKey key, Provider provider) throws CMSException
            {
                // TODO Create AEAD cipher instance to decrypt and calculate tag ( MAC)
                throw new CMSException("AuthEnveloped data decryption not yet implemented");

//              RFC 5084 ASN.1 Module
//                -- Parameters for AlgorithmIdentifier
//
//                CCMParameters ::= SEQUENCE {
//                  aes-nonce         OCTET STRING (SIZE(7..13)),
//                  aes-ICVlen        AES-CCM-ICVlen DEFAULT 12 }
//
//                AES-CCM-ICVlen ::= INTEGER (4 | 6 | 8 | 10 | 12 | 14 | 16)
//
//                GCMParameters ::= SEQUENCE {
//                  aes-nonce        OCTET STRING, -- recommended size is 12 octets
//                  aes-ICVlen       AES-GCM-ICVlen DEFAULT 12 }
//
//                AES-GCM-ICVlen ::= INTEGER (12 | 13 | 14 | 15 | 16)
            }

            public InputStream getInputStream()
                throws IOException, CMSException
            {
                return null;
            }
        };

        //
        // build the RecipientInformationStore
        //
        this.recipientInfoStore = CMSEnvelopedHelper.buildRecipientInformationStore(
            recipientInfos, this.authEncAlg, secureReadable);

        // FIXME These need to be passed to the AEAD cipher as AAD (Additional Authenticated Data)
        this.authAttrs = authEnvData.getAuthAttrs();
        this.mac = authEnvData.getMac().getOctets();
        this.unauthAttrs = authEnvData.getUnauthAttrs();
    }
}
