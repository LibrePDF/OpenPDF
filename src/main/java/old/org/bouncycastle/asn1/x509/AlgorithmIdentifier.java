package old.org.bouncycastle.asn1.x509;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;

public class AlgorithmIdentifier
    extends ASN1Encodable
{
    private DERObjectIdentifier objectId;
    private DEREncodable        parameters;
    private boolean             parametersDefined = false;

    public static AlgorithmIdentifier getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }
    
    public static AlgorithmIdentifier getInstance(
        Object  obj)
    {
        if (obj== null || obj instanceof AlgorithmIdentifier)
        {
            return (AlgorithmIdentifier)obj;
        }
        
        if (obj instanceof DERObjectIdentifier)
        {
            return new AlgorithmIdentifier((DERObjectIdentifier)obj);
        }

        if (obj instanceof String)
        {
            return new AlgorithmIdentifier((String)obj);
        }

        if (obj instanceof ASN1Sequence)
        {
            return new AlgorithmIdentifier((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public AlgorithmIdentifier(
        DERObjectIdentifier     objectId)
    {
        this.objectId = objectId;
    }

    public AlgorithmIdentifier(
        String     objectId)
    {
        this.objectId = new DERObjectIdentifier(objectId);
    }

    public AlgorithmIdentifier(
        DERObjectIdentifier     objectId,
        DEREncodable            parameters)
    {
        parametersDefined = true;
        this.objectId = objectId;
        this.parameters = parameters;
    }

    public AlgorithmIdentifier(
        ASN1Sequence   seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }
        
        objectId = DERObjectIdentifier.getInstance(seq.getObjectAt(0));

        if (seq.size() == 2)
        {
            parametersDefined = true;
            parameters = seq.getObjectAt(1);
        }
        else
        {
            parameters = null;
        }
    }

    public ASN1ObjectIdentifier getAlgorithm()
    {
        return new ASN1ObjectIdentifier(objectId.getId());
    }

    /**
     * @deprecated use getAlgorithm
     * @return
     */
    public DERObjectIdentifier getObjectId()
    {
        return objectId;
    }

    public DEREncodable getParameters()
    {
        return parameters;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     *      AlgorithmIdentifier ::= SEQUENCE {
     *                            algorithm OBJECT IDENTIFIER,
     *                            parameters ANY DEFINED BY algorithm OPTIONAL }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(objectId);

        if (parametersDefined)
        {
            if (parameters != null)
            {
                v.add(parameters);
            }
            else
            {
                v.add(DERNull.INSTANCE);
            }
        }

        return new DERSequence(v);
    }
}
