package old.org.bouncycastle.asn1.x509.sigi;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Object Identifiers of SigI specifciation (German Signature Law
 * Interoperability specification).
 */
public interface SigIObjectIdentifiers
{
    public final static ASN1ObjectIdentifier id_sigi = new ASN1ObjectIdentifier("1.3.36.8");

    /**
     * Key purpose IDs for German SigI (Signature Interoperability
     * Specification)
     */
    public final static ASN1ObjectIdentifier id_sigi_kp = new ASN1ObjectIdentifier(id_sigi + ".2");

    /**
     * Certificate policy IDs for German SigI (Signature Interoperability
     * Specification)
     */
    public final static ASN1ObjectIdentifier id_sigi_cp = new ASN1ObjectIdentifier(id_sigi + ".1");

    /**
     * Other Name IDs for German SigI (Signature Interoperability Specification)
     */
    public final static ASN1ObjectIdentifier id_sigi_on = new ASN1ObjectIdentifier(id_sigi + ".4");

    /**
     * To be used for for the generation of directory service certificates.
     */
    public static final ASN1ObjectIdentifier id_sigi_kp_directoryService = new ASN1ObjectIdentifier(id_sigi_kp + ".1");

    /**
     * ID for PersonalData
     */
    public static final ASN1ObjectIdentifier id_sigi_on_personalData = new ASN1ObjectIdentifier(id_sigi_on + ".1");

    /**
     * Certificate is conform to german signature law.
     */
    public static final ASN1ObjectIdentifier id_sigi_cp_sigconform = new ASN1ObjectIdentifier(id_sigi_cp + ".1");

}
