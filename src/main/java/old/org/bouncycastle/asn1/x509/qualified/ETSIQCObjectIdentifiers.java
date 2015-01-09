package old.org.bouncycastle.asn1.x509.qualified;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface ETSIQCObjectIdentifiers
{
    //
    // base id
    //
    static final ASN1ObjectIdentifier    id_etsi_qcs                  = new ASN1ObjectIdentifier("0.4.0.1862.1");

    static final ASN1ObjectIdentifier    id_etsi_qcs_QcCompliance     = id_etsi_qcs.branch("1");
    static final ASN1ObjectIdentifier    id_etsi_qcs_LimiteValue      = id_etsi_qcs.branch("2");
    static final ASN1ObjectIdentifier    id_etsi_qcs_RetentionPeriod  = id_etsi_qcs.branch("3");
    static final ASN1ObjectIdentifier    id_etsi_qcs_QcSSCD           = id_etsi_qcs.branch("4");
}
