package old.org.bouncycastle.jce;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.x509.KeyUsage;

/**
 * A holding class for constructing an X509 Key Usage extension.
 *
 * <pre>
 *    id-ce-keyUsage OBJECT IDENTIFIER ::=  { id-ce 15 }
 *
 *    KeyUsage ::= BIT STRING {
 *         digitalSignature        (0),
 *         nonRepudiation          (1),
 *         keyEncipherment         (2),
 *         dataEncipherment        (3),
 *         keyAgreement            (4),
 *         keyCertSign             (5),
 *         cRLSign                 (6),
 *         encipherOnly            (7),
 *         decipherOnly            (8) }
 * </pre>
 */
public class X509KeyUsage
    extends ASN1Encodable
{
    public static final int        digitalSignature = 1 << 7; 
    public static final int        nonRepudiation   = 1 << 6;
    public static final int        keyEncipherment  = 1 << 5;
    public static final int        dataEncipherment = 1 << 4;
    public static final int        keyAgreement     = 1 << 3;
    public static final int        keyCertSign      = 1 << 2;
    public static final int        cRLSign          = 1 << 1;
    public static final int        encipherOnly     = 1 << 0;
    public static final int        decipherOnly     = 1 << 15;

    private int usage = 0;

    /**
     * Basic constructor.
     * 
     * @param usage - the bitwise OR of the Key Usage flags giving the
     * allowed uses for the key.
     * e.g. (X509KeyUsage.keyEncipherment | X509KeyUsage.dataEncipherment)
     */
    public X509KeyUsage(
        int usage)
    {
        this.usage = usage;
    }

    public DERObject toASN1Object()
    {
        return new KeyUsage(usage);
    }
}
