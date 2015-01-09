package old.org.bouncycastle.asn1.cmp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class PBMParameter
    extends ASN1Encodable
{
    private ASN1OctetString salt;
    private AlgorithmIdentifier owf;
    private DERInteger iterationCount;
    private AlgorithmIdentifier mac;

    private PBMParameter(ASN1Sequence seq)
    {
        salt = ASN1OctetString.getInstance(seq.getObjectAt(0));
        owf = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        iterationCount = DERInteger.getInstance(seq.getObjectAt(2));
        mac = AlgorithmIdentifier.getInstance(seq.getObjectAt(3));
    }

    public static PBMParameter getInstance(Object o)
    {
        if (o instanceof PBMParameter)
        {
            return (PBMParameter)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new PBMParameter((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public PBMParameter(
        byte[] salt,
        AlgorithmIdentifier owf,
        int iterationCount,
        AlgorithmIdentifier mac)
    {
        this(new DEROctetString(salt), owf,
             new DERInteger(iterationCount), mac);
    }

    public PBMParameter(
        ASN1OctetString salt,
        AlgorithmIdentifier owf,
        DERInteger iterationCount,
        AlgorithmIdentifier mac)
    {
        this.salt = salt;
        this.owf = owf;
        this.iterationCount = iterationCount;
        this.mac = mac;
    }

    public ASN1OctetString getSalt()
    {
        return salt;
    }

    public AlgorithmIdentifier getOwf()
    {
        return owf;
    }

    public DERInteger getIterationCount()
    {
        return iterationCount;
    }

    public AlgorithmIdentifier getMac()
    {
        return mac;
    }

    /**
     * <pre>
     *  PBMParameter ::= SEQUENCE {
     *                        salt                OCTET STRING,
     *                        -- note:  implementations MAY wish to limit acceptable sizes
     *                        -- of this string to values appropriate for their environment
     *                        -- in order to reduce the risk of denial-of-service attacks
     *                        owf                 AlgorithmIdentifier,
     *                        -- AlgId for a One-Way Function (SHA-1 recommended)
     *                        iterationCount      INTEGER,
     *                        -- number of times the OWF is applied
     *                        -- note:  implementations MAY wish to limit acceptable sizes
     *                        -- of this integer to values appropriate for their environment
     *                        -- in order to reduce the risk of denial-of-service attacks
     *                        mac                 AlgorithmIdentifier
     *                        -- the MAC AlgId (e.g., DES-MAC, Triple-DES-MAC [PKCS11],
     *    }   -- or HMAC [RFC2104, RFC2202])
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(salt);
        v.add(owf);
        v.add(iterationCount);
        v.add(mac);
        
        return new DERSequence(v);
    }
}
