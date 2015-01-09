package old.org.bouncycastle.cert.crmf;

import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.crmf.PKMACValue;
import old.org.bouncycastle.asn1.crmf.POPOSigningKey;
import old.org.bouncycastle.asn1.crmf.POPOSigningKeyInput;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.operator.ContentSigner;

public class ProofOfPossessionSigningKeyBuilder
{
    private SubjectPublicKeyInfo pubKeyInfo;
    private GeneralName name;
    private PKMACValue publicKeyMAC;

    public ProofOfPossessionSigningKeyBuilder(SubjectPublicKeyInfo pubKeyInfo)
    {
        this.pubKeyInfo = pubKeyInfo;
    }

    public ProofOfPossessionSigningKeyBuilder setSender(GeneralName name)
    {
        this.name = name;

        return this;
    }

    public ProofOfPossessionSigningKeyBuilder setPublicKeyMac(PKMACValueGenerator generator, char[] password)
        throws CRMFException
    {
        this.publicKeyMAC = generator.generate(password, pubKeyInfo);

        return this;
    }

    public POPOSigningKey build(ContentSigner signer)
    {
        if (name != null && publicKeyMAC != null)
        {
            throw new IllegalStateException("name and publicKeyMAC cannot both be set.");
        }

        POPOSigningKeyInput popo;

        if (name != null)
        {
            popo = new POPOSigningKeyInput(name, pubKeyInfo);
        }
        else
        {
            popo = new POPOSigningKeyInput(publicKeyMAC, pubKeyInfo);
        }

        CRMFUtil.derEncodeToStream(popo, signer.getOutputStream());

        return new POPOSigningKey(popo, signer.getAlgorithmIdentifier(), new DERBitString(signer.getSignature()));
    }
}
