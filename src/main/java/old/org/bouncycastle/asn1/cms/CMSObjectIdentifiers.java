package old.org.bouncycastle.asn1.cms;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

public interface CMSObjectIdentifiers
{
    static final ASN1ObjectIdentifier    data = PKCSObjectIdentifiers.data;
    static final ASN1ObjectIdentifier    signedData = PKCSObjectIdentifiers.signedData;
    static final ASN1ObjectIdentifier    envelopedData = PKCSObjectIdentifiers.envelopedData;
    static final ASN1ObjectIdentifier    signedAndEnvelopedData = PKCSObjectIdentifiers.signedAndEnvelopedData;
    static final ASN1ObjectIdentifier    digestedData = PKCSObjectIdentifiers.digestedData;
    static final ASN1ObjectIdentifier    encryptedData = PKCSObjectIdentifiers.encryptedData;
    static final ASN1ObjectIdentifier    authenticatedData = PKCSObjectIdentifiers.id_ct_authData;
    static final ASN1ObjectIdentifier    compressedData = PKCSObjectIdentifiers.id_ct_compressedData;
    static final ASN1ObjectIdentifier    authEnvelopedData = PKCSObjectIdentifiers.id_ct_authEnvelopedData;
    static final ASN1ObjectIdentifier    timestampedData = PKCSObjectIdentifiers.id_ct_timestampedData;
}
