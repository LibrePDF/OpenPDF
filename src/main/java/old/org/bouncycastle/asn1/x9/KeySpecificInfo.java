package old.org.bouncycastle.asn1.x9;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;

/**
 * ASN.1 def for Diffie-Hellman key exchange KeySpecificInfo structure. See
 * RFC 2631, or X9.42, for further details.
 */
public class KeySpecificInfo
    extends ASN1Encodable
{
    private DERObjectIdentifier algorithm;
    private ASN1OctetString      counter;

    public KeySpecificInfo(
        DERObjectIdentifier algorithm,
        ASN1OctetString      counter)
    {
        this.algorithm = algorithm;
        this.counter = counter;
    }

    public KeySpecificInfo(
        ASN1Sequence  seq)
    {
        Enumeration e = seq.getObjects();

        algorithm = (DERObjectIdentifier)e.nextElement();
        counter = (ASN1OctetString)e.nextElement();
    }

    public DERObjectIdentifier getAlgorithm()
    {
        return algorithm;
    }

    public ASN1OctetString getCounter()
    {
        return counter;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     *  KeySpecificInfo ::= SEQUENCE {
     *      algorithm OBJECT IDENTIFIER,
     *      counter OCTET STRING SIZE (4..4)
     *  }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(algorithm);
        v.add(counter);

        return new DERSequence(v);
    }
}
