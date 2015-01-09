package old.org.bouncycastle.asn1.ocsp;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface OCSPObjectIdentifiers
{
    public static final String pkix_ocsp = "1.3.6.1.5.5.7.48.1";

    public static final ASN1ObjectIdentifier id_pkix_ocsp = new ASN1ObjectIdentifier(pkix_ocsp);
    public static final ASN1ObjectIdentifier id_pkix_ocsp_basic = new ASN1ObjectIdentifier(pkix_ocsp + ".1");
    
    //
    // extensions
    //
    public static final ASN1ObjectIdentifier id_pkix_ocsp_nonce = new ASN1ObjectIdentifier(pkix_ocsp + ".2");
    public static final ASN1ObjectIdentifier id_pkix_ocsp_crl = new ASN1ObjectIdentifier(pkix_ocsp + ".3");
    
    public static final ASN1ObjectIdentifier id_pkix_ocsp_response = new ASN1ObjectIdentifier(pkix_ocsp + ".4");
    public static final ASN1ObjectIdentifier id_pkix_ocsp_nocheck = new ASN1ObjectIdentifier(pkix_ocsp + ".5");
    public static final ASN1ObjectIdentifier id_pkix_ocsp_archive_cutoff = new ASN1ObjectIdentifier(pkix_ocsp + ".6");
    public static final ASN1ObjectIdentifier id_pkix_ocsp_service_locator = new ASN1ObjectIdentifier(pkix_ocsp + ".7");
}
