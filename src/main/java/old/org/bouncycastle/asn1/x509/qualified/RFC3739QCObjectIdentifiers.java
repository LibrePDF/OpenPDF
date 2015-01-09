package old.org.bouncycastle.asn1.x509.qualified;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface RFC3739QCObjectIdentifiers
{
    //
    // base id
    //
    static final ASN1ObjectIdentifier   id_qcs             = new ASN1ObjectIdentifier("1.3.6.1.5.5.7.11");

    static final ASN1ObjectIdentifier   id_qcs_pkixQCSyntax_v1  = id_qcs.branch("1");
    static final ASN1ObjectIdentifier   id_qcs_pkixQCSyntax_v2  = id_qcs.branch("2");
}
