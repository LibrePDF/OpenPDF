package old.org.bouncycastle.asn1.pkcs;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class PBES2Parameters
    extends ASN1Encodable
    implements PKCSObjectIdentifiers
{
    private KeyDerivationFunc   func;
    private EncryptionScheme    scheme;

    public static PBES2Parameters getInstance(
        Object  obj)
    {
        if (obj== null || obj instanceof PBES2Parameters)
        {
            return (PBES2Parameters)obj;
        }

        if (obj instanceof ASN1Sequence)
        {
            return new PBES2Parameters((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public PBES2Parameters(
        ASN1Sequence  obj)
    {
        Enumeration e = obj.getObjects();
        ASN1Sequence  funcSeq = ASN1Sequence.getInstance(((DEREncodable)e.nextElement()).getDERObject());

        if (funcSeq.getObjectAt(0).equals(id_PBKDF2))
        {
            func = new KeyDerivationFunc(id_PBKDF2, PBKDF2Params.getInstance(funcSeq.getObjectAt(1)));
        }
        else
        {
            func = new KeyDerivationFunc(funcSeq);
        }

        scheme = (EncryptionScheme)EncryptionScheme.getInstance(e.nextElement());
    }

    public KeyDerivationFunc getKeyDerivationFunc()
    {
        return func;
    }

    public EncryptionScheme getEncryptionScheme()
    {
        return scheme;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(func);
        v.add(scheme);

        return new DERSequence(v);
    }
}
