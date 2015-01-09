package old.org.bouncycastle.asn1.icao;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface ICAOObjectIdentifiers
{
    //
    // base id
    //
    static final ASN1ObjectIdentifier    id_icao                   = new ASN1ObjectIdentifier("2.23.136");

    static final ASN1ObjectIdentifier    id_icao_mrtd              = id_icao.branch("1");
    static final ASN1ObjectIdentifier    id_icao_mrtd_security     = id_icao_mrtd.branch("1");

    // LDS security object, see ICAO Doc 9303-Volume 2-Section IV-A3.2
    static final ASN1ObjectIdentifier    id_icao_ldsSecurityObject = id_icao_mrtd_security.branch("1");

    // CSCA master list, see TR CSCA Countersigning and Master List issuance
    static final ASN1ObjectIdentifier    id_icao_cscaMasterList    = id_icao_mrtd_security.branch("2");
    static final ASN1ObjectIdentifier    id_icao_cscaMasterListSigningKey = id_icao_mrtd_security.branch("3");

    // document type list, see draft TR LDS and PKI Maintenance, par. 3.2.1
    static final ASN1ObjectIdentifier    id_icao_documentTypeList  = id_icao_mrtd_security.branch("4");

    // Active Authentication protocol, see draft TR LDS and PKI Maintenance,
    // par. 5.2.2
    static final ASN1ObjectIdentifier    id_icao_aaProtocolObject  = id_icao_mrtd_security.branch("5");

    // CSCA name change and key reoll-over, see draft TR LDS and PKI
    // Maintenance, par. 3.2.1
    static final ASN1ObjectIdentifier    id_icao_extensions        = id_icao_mrtd_security.branch("6");
    static final ASN1ObjectIdentifier    id_icao_extensions_namechangekeyrollover = id_icao_extensions.branch("1");
}
