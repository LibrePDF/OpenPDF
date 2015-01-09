package old.org.bouncycastle.asn1;

public class ASN1ObjectIdentifier
    extends DERObjectIdentifier
{
    public ASN1ObjectIdentifier(String identifier)
    {
        super(identifier);
    }

    ASN1ObjectIdentifier(byte[] bytes)
    {
        super(bytes);
    }

    /**
     * Return an OID that creates a branch under the current one.
     *
     * @param branchID node numbers for the new branch.
     * @return
     */
    public ASN1ObjectIdentifier branch(String branchID)
    {
        return new ASN1ObjectIdentifier(getId() + "." + branchID);
    }
}
