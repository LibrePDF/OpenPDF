package old.org.bouncycastle.tsp;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Vector;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.DERBoolean;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.tsp.MessageImprint;
import old.org.bouncycastle.asn1.tsp.TimeStampReq;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;

/**
 * Generator for RFC 3161 Time Stamp Request objects.
 */
public class TimeStampRequestGenerator
{
    private DERObjectIdentifier reqPolicy;

    private DERBoolean certReq;
    
    private Hashtable   extensions = new Hashtable();
    private Vector      extOrdering = new Vector();

    public TimeStampRequestGenerator()
    {
    }

    public void setReqPolicy(
        String reqPolicy)
    {
        this.reqPolicy= new DERObjectIdentifier(reqPolicy);
    }

    public void setCertReq(
        boolean certReq)
    {
        this.certReq = new DERBoolean(certReq);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     * @throws IOException
     * @deprecated use method taking ASN1ObjectIdentifier
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        ASN1Encodable   value)
        throws IOException
    {
        this.addExtension(OID, critical, value.getEncoded());
    }

    /**
     * add a given extension field for the standard extensions tag
     * The value parameter becomes the contents of the octet string associated
     * with the extension.
     * @deprecated use method taking ASN1ObjectIdentifier
     */
    public void addExtension(
        String          OID,
        boolean         critical,
        byte[]          value)
    {
        DERObjectIdentifier oid = new DERObjectIdentifier(OID);
        extensions.put(oid, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(oid);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     * @throws IOException
     */
    public void addExtension(
        ASN1ObjectIdentifier oid,
        boolean              critical,
        ASN1Encodable        value)
        throws IOException
    {
        this.addExtension(oid, critical, value.getEncoded());
    }

    /**
     * add a given extension field for the standard extensions tag
     * The value parameter becomes the contents of the octet string associated
     * with the extension.
     */
    public void addExtension(
        ASN1ObjectIdentifier oid,
        boolean              critical,
        byte[]               value)
    {
        extensions.put(oid, new X509Extension(critical, new DEROctetString(value)));
        extOrdering.addElement(oid);
    }

    public TimeStampRequest generate(
        String digestAlgorithm,
        byte[] digest)
    {
        return this.generate(digestAlgorithm, digest, null);
    }

    public TimeStampRequest generate(
        String      digestAlgorithmOID,
        byte[]      digest,
        BigInteger  nonce)
    {
        if (digestAlgorithmOID == null)
        {
            throw new IllegalArgumentException("No digest algorithm specified");
        }

        DERObjectIdentifier digestAlgOID = new DERObjectIdentifier(digestAlgorithmOID);

        AlgorithmIdentifier algID = new AlgorithmIdentifier(digestAlgOID, new DERNull());
        MessageImprint messageImprint = new MessageImprint(algID, digest);

        X509Extensions  ext = null;
        
        if (extOrdering.size() != 0)
        {
            ext = new X509Extensions(extOrdering, extensions);
        }
        
        if (nonce != null)
        {
            return new TimeStampRequest(new TimeStampReq(messageImprint,
                    reqPolicy, new DERInteger(nonce), certReq, ext));
        }
        else
        {
            return new TimeStampRequest(new TimeStampReq(messageImprint,
                    reqPolicy, null, certReq, ext));
        }
    }

    public TimeStampRequest generate(ASN1ObjectIdentifier digestAlgorithm, byte[] digest, BigInteger nonce)
    {
        return generate(digestAlgorithm.getId(), digest, nonce);
    }
}
