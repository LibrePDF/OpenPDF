package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cms.SignerInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

interface SignerIntInfoGenerator
{
    SignerInfo generate(DERObjectIdentifier contentType, AlgorithmIdentifier digestAlgorithm,
        byte[] calculatedDigest) throws CMSStreamException;
}
