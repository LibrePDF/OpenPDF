package old.org.bouncycastle.asn1.ntt;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * From RFC 3657
 */
public interface NTTObjectIdentifiers
{
    public static final ASN1ObjectIdentifier id_camellia128_cbc = new ASN1ObjectIdentifier("1.2.392.200011.61.1.1.1.2");
    public static final ASN1ObjectIdentifier id_camellia192_cbc = new ASN1ObjectIdentifier("1.2.392.200011.61.1.1.1.3");
    public static final ASN1ObjectIdentifier id_camellia256_cbc = new ASN1ObjectIdentifier("1.2.392.200011.61.1.1.1.4");

    public static final ASN1ObjectIdentifier id_camellia128_wrap = new ASN1ObjectIdentifier("1.2.392.200011.61.1.1.3.2");
    public static final ASN1ObjectIdentifier id_camellia192_wrap = new ASN1ObjectIdentifier("1.2.392.200011.61.1.1.3.3");
    public static final ASN1ObjectIdentifier id_camellia256_wrap = new ASN1ObjectIdentifier("1.2.392.200011.61.1.1.3.4");
}
