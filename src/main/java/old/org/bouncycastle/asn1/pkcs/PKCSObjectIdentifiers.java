package old.org.bouncycastle.asn1.pkcs;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface PKCSObjectIdentifiers
{
    //
    // pkcs-1 OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 1 }
    //
    static final ASN1ObjectIdentifier    pkcs_1                    = new ASN1ObjectIdentifier("1.2.840.113549.1.1");
    static final ASN1ObjectIdentifier    rsaEncryption             = pkcs_1.branch("1");
    static final ASN1ObjectIdentifier    md2WithRSAEncryption      = pkcs_1.branch("2");
    static final ASN1ObjectIdentifier    md4WithRSAEncryption      = pkcs_1.branch("3");
    static final ASN1ObjectIdentifier    md5WithRSAEncryption      = pkcs_1.branch("4");
    static final ASN1ObjectIdentifier    sha1WithRSAEncryption     = pkcs_1.branch("5");
    static final ASN1ObjectIdentifier    srsaOAEPEncryptionSET     = pkcs_1.branch("6");
    static final ASN1ObjectIdentifier    id_RSAES_OAEP             = pkcs_1.branch("7");
    static final ASN1ObjectIdentifier    id_mgf1                   = pkcs_1.branch("8");
    static final ASN1ObjectIdentifier    id_pSpecified             = pkcs_1.branch("9");
    static final ASN1ObjectIdentifier    id_RSASSA_PSS             = pkcs_1.branch("10");
    static final ASN1ObjectIdentifier    sha256WithRSAEncryption   = pkcs_1.branch("11");
    static final ASN1ObjectIdentifier    sha384WithRSAEncryption   = pkcs_1.branch("12");
    static final ASN1ObjectIdentifier    sha512WithRSAEncryption   = pkcs_1.branch("13");
    static final ASN1ObjectIdentifier    sha224WithRSAEncryption   = pkcs_1.branch("14");

    //
    // pkcs-3 OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 3 }
    //
    static final ASN1ObjectIdentifier    pkcs_3                  = new ASN1ObjectIdentifier("1.2.840.113549.1.3");
    static final ASN1ObjectIdentifier    dhKeyAgreement          = pkcs_3.branch("1");

    //
    // pkcs-5 OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 5 }
    //
    static final ASN1ObjectIdentifier    pkcs_5                  = new ASN1ObjectIdentifier("1.2.840.113549.1.5");

    static final ASN1ObjectIdentifier    pbeWithMD2AndDES_CBC    = pkcs_5.branch("1");
    static final ASN1ObjectIdentifier    pbeWithMD2AndRC2_CBC    = pkcs_5.branch("4");
    static final ASN1ObjectIdentifier    pbeWithMD5AndDES_CBC    = pkcs_5.branch("3");
    static final ASN1ObjectIdentifier    pbeWithMD5AndRC2_CBC    = pkcs_5.branch("6");
    static final ASN1ObjectIdentifier    pbeWithSHA1AndDES_CBC   = pkcs_5.branch("10");
    static final ASN1ObjectIdentifier    pbeWithSHA1AndRC2_CBC   = pkcs_5.branch("11");

    static final ASN1ObjectIdentifier    id_PBES2                = pkcs_5.branch("13");

    static final ASN1ObjectIdentifier    id_PBKDF2               = pkcs_5.branch("12");

    //
    // encryptionAlgorithm OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) 3 }
    //
    static final ASN1ObjectIdentifier    encryptionAlgorithm     = new ASN1ObjectIdentifier("1.2.840.113549.3");

    static final ASN1ObjectIdentifier    des_EDE3_CBC            = encryptionAlgorithm.branch("7");
    static final ASN1ObjectIdentifier    RC2_CBC                 = encryptionAlgorithm.branch("2");

    //
    // object identifiers for digests
    //
    static final ASN1ObjectIdentifier    digestAlgorithm        = new ASN1ObjectIdentifier("1.2.840.113549.2");
    //
    // md2 OBJECT IDENTIFIER ::=
    //      {iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) 2}
    //
    static final ASN1ObjectIdentifier    md2                    = digestAlgorithm.branch("2");

    //
    // md4 OBJECT IDENTIFIER ::=
    //      {iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) 4}
    //
    static final ASN1ObjectIdentifier    md4 = digestAlgorithm.branch("4");

    //
    // md5 OBJECT IDENTIFIER ::=
    //      {iso(1) member-body(2) US(840) rsadsi(113549) digestAlgorithm(2) 5}
    //
    static final ASN1ObjectIdentifier    md5                     = digestAlgorithm.branch("5");

    static final ASN1ObjectIdentifier    id_hmacWithSHA1         = digestAlgorithm.branch("7");
    static final ASN1ObjectIdentifier    id_hmacWithSHA224       = digestAlgorithm.branch("8");
    static final ASN1ObjectIdentifier    id_hmacWithSHA256       = digestAlgorithm.branch("9");
    static final ASN1ObjectIdentifier    id_hmacWithSHA384       = digestAlgorithm.branch("10");
    static final ASN1ObjectIdentifier    id_hmacWithSHA512       = digestAlgorithm.branch("11");

    //
    // pkcs-7 OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 7 }
    //
    static final String                 pkcs_7                  = "1.2.840.113549.1.7";
    static final ASN1ObjectIdentifier    data                    = new ASN1ObjectIdentifier(pkcs_7 + ".1");
    static final ASN1ObjectIdentifier    signedData              = new ASN1ObjectIdentifier(pkcs_7 + ".2");
    static final ASN1ObjectIdentifier    envelopedData           = new ASN1ObjectIdentifier(pkcs_7 + ".3");
    static final ASN1ObjectIdentifier    signedAndEnvelopedData  = new ASN1ObjectIdentifier(pkcs_7 + ".4");
    static final ASN1ObjectIdentifier    digestedData            = new ASN1ObjectIdentifier(pkcs_7 + ".5");
    static final ASN1ObjectIdentifier    encryptedData           = new ASN1ObjectIdentifier(pkcs_7 + ".6");

    //
    // pkcs-9 OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 9 }
    //
    static final ASN1ObjectIdentifier    pkcs_9                  = new ASN1ObjectIdentifier("1.2.840.113549.1.9");

    static final ASN1ObjectIdentifier    pkcs_9_at_emailAddress  = pkcs_9.branch("1");
    static final ASN1ObjectIdentifier    pkcs_9_at_unstructuredName = pkcs_9.branch("2");
    static final ASN1ObjectIdentifier    pkcs_9_at_contentType = pkcs_9.branch("3");
    static final ASN1ObjectIdentifier    pkcs_9_at_messageDigest = pkcs_9.branch("4");
    static final ASN1ObjectIdentifier    pkcs_9_at_signingTime = pkcs_9.branch("5");
    static final ASN1ObjectIdentifier    pkcs_9_at_counterSignature = pkcs_9.branch("6");
    static final ASN1ObjectIdentifier    pkcs_9_at_challengePassword = pkcs_9.branch("7");
    static final ASN1ObjectIdentifier    pkcs_9_at_unstructuredAddress = pkcs_9.branch("8");
    static final ASN1ObjectIdentifier    pkcs_9_at_extendedCertificateAttributes = pkcs_9.branch("9");

    static final ASN1ObjectIdentifier    pkcs_9_at_signingDescription = pkcs_9.branch("13");
    static final ASN1ObjectIdentifier    pkcs_9_at_extensionRequest = pkcs_9.branch("14");
    static final ASN1ObjectIdentifier    pkcs_9_at_smimeCapabilities = pkcs_9.branch("15");

    static final ASN1ObjectIdentifier    pkcs_9_at_friendlyName  = pkcs_9.branch("20");
    static final ASN1ObjectIdentifier    pkcs_9_at_localKeyId    = pkcs_9.branch("21");

    /** @deprecated use x509Certificate instead */
    static final ASN1ObjectIdentifier    x509certType            = pkcs_9.branch("22.1");

    static final ASN1ObjectIdentifier    certTypes               = pkcs_9.branch("22");
    static final ASN1ObjectIdentifier    x509Certificate         = certTypes.branch("1");
    static final ASN1ObjectIdentifier    sdsiCertificate         = certTypes.branch("2");

    static final ASN1ObjectIdentifier    crlTypes                = pkcs_9.branch("23");
    static final ASN1ObjectIdentifier    x509Crl                 = crlTypes.branch("1");

    static final ASN1ObjectIdentifier    id_alg_PWRI_KEK    = pkcs_9.branch("16.3.9");

    //
    // SMIME capability sub oids.
    //
    static final ASN1ObjectIdentifier    preferSignedData        = pkcs_9.branch("15.1");
    static final ASN1ObjectIdentifier    canNotDecryptAny        = pkcs_9.branch("15.2");
    static final ASN1ObjectIdentifier    sMIMECapabilitiesVersions = pkcs_9.branch("15.3");

    //
    // id-ct OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
    // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) ct(1)}
    //
    static final ASN1ObjectIdentifier    id_ct = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.1");

    static final ASN1ObjectIdentifier    id_ct_authData          = id_ct.branch("2");
    static final ASN1ObjectIdentifier    id_ct_TSTInfo           = id_ct.branch("4");
    static final ASN1ObjectIdentifier    id_ct_compressedData    = id_ct.branch("9");
    static final ASN1ObjectIdentifier    id_ct_authEnvelopedData = id_ct.branch("23");
    static final ASN1ObjectIdentifier    id_ct_timestampedData   = id_ct.branch("31");

    //
    // id-cti OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
    // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) cti(6)}
    //
    static final ASN1ObjectIdentifier    id_cti = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.6");
    
    static final ASN1ObjectIdentifier    id_cti_ets_proofOfOrigin  = id_cti.branch("1");
    static final ASN1ObjectIdentifier    id_cti_ets_proofOfReceipt = id_cti.branch("2");
    static final ASN1ObjectIdentifier    id_cti_ets_proofOfDelivery = id_cti.branch("3");
    static final ASN1ObjectIdentifier    id_cti_ets_proofOfSender = id_cti.branch("4");
    static final ASN1ObjectIdentifier    id_cti_ets_proofOfApproval = id_cti.branch("5");
    static final ASN1ObjectIdentifier    id_cti_ets_proofOfCreation = id_cti.branch("6");
    
    //
    // id-aa OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
    // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) attributes(2)}
    //
    static final ASN1ObjectIdentifier    id_aa = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2");


    static final ASN1ObjectIdentifier id_aa_receiptRequest = id_aa.branch("1");
    
    static final ASN1ObjectIdentifier id_aa_contentHint = id_aa.branch("4"); // See RFC 2634
    static final ASN1ObjectIdentifier id_aa_msgSigDigest = id_aa.branch("5");
    static final ASN1ObjectIdentifier id_aa_contentReference = id_aa.branch("10");
    /*
     * id-aa-encrypKeyPref OBJECT IDENTIFIER ::= {id-aa 11}
     * 
     */
    static final ASN1ObjectIdentifier id_aa_encrypKeyPref = id_aa.branch("11");
    static final ASN1ObjectIdentifier id_aa_signingCertificate = id_aa.branch("12");
    static final ASN1ObjectIdentifier id_aa_signingCertificateV2 = id_aa.branch("47");

    static final ASN1ObjectIdentifier id_aa_contentIdentifier = id_aa.branch("7"); // See RFC 2634

    /*
     * RFC 3126
     */
    static final ASN1ObjectIdentifier id_aa_signatureTimeStampToken = id_aa.branch("14");
    
    static final ASN1ObjectIdentifier id_aa_ets_sigPolicyId = id_aa.branch("15");
    static final ASN1ObjectIdentifier id_aa_ets_commitmentType = id_aa.branch("16");
    static final ASN1ObjectIdentifier id_aa_ets_signerLocation = id_aa.branch("17");
    static final ASN1ObjectIdentifier id_aa_ets_signerAttr = id_aa.branch("18");
    static final ASN1ObjectIdentifier id_aa_ets_otherSigCert = id_aa.branch("19");
    static final ASN1ObjectIdentifier id_aa_ets_contentTimestamp = id_aa.branch("20");
    static final ASN1ObjectIdentifier id_aa_ets_certificateRefs = id_aa.branch("21");
    static final ASN1ObjectIdentifier id_aa_ets_revocationRefs = id_aa.branch("22");
    static final ASN1ObjectIdentifier id_aa_ets_certValues = id_aa.branch("23");
    static final ASN1ObjectIdentifier id_aa_ets_revocationValues = id_aa.branch("24");
    static final ASN1ObjectIdentifier id_aa_ets_escTimeStamp = id_aa.branch("25");
    static final ASN1ObjectIdentifier id_aa_ets_certCRLTimestamp = id_aa.branch("26");
    static final ASN1ObjectIdentifier id_aa_ets_archiveTimestamp = id_aa.branch("27");

    /** @deprecated use id_aa_ets_sigPolicyId instead */
    static final ASN1ObjectIdentifier id_aa_sigPolicyId = id_aa_ets_sigPolicyId;
    /** @deprecated use id_aa_ets_commitmentType instead */
    static final ASN1ObjectIdentifier id_aa_commitmentType = id_aa_ets_commitmentType;
    /** @deprecated use id_aa_ets_signerLocation instead */
    static final ASN1ObjectIdentifier id_aa_signerLocation = id_aa_ets_signerLocation;
    /** @deprecated use id_aa_ets_otherSigCert instead */
    static final ASN1ObjectIdentifier id_aa_otherSigCert = id_aa_ets_otherSigCert;
    
    //
    // id-spq OBJECT IDENTIFIER ::= {iso(1) member-body(2) usa(840)
    // rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) id-spq(5)}
    //
    final String id_spq = "1.2.840.113549.1.9.16.5";

    static final ASN1ObjectIdentifier id_spq_ets_uri = new ASN1ObjectIdentifier(id_spq + ".1");
    static final ASN1ObjectIdentifier id_spq_ets_unotice = new ASN1ObjectIdentifier(id_spq + ".2");

    //
    // pkcs-12 OBJECT IDENTIFIER ::= {
    //       iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1) 12 }
    //
    static final ASN1ObjectIdentifier   pkcs_12                  = new ASN1ObjectIdentifier("1.2.840.113549.1.12");
    static final ASN1ObjectIdentifier   bagtypes                 = pkcs_12.branch("10.1");

    static final ASN1ObjectIdentifier    keyBag                  = bagtypes.branch("1");
    static final ASN1ObjectIdentifier    pkcs8ShroudedKeyBag     = bagtypes.branch("2");
    static final ASN1ObjectIdentifier    certBag                 = bagtypes.branch("3");
    static final ASN1ObjectIdentifier    crlBag                  = bagtypes.branch("4");
    static final ASN1ObjectIdentifier    secretBag               = bagtypes.branch("5");
    static final ASN1ObjectIdentifier    safeContentsBag         = bagtypes.branch("6");

    static final ASN1ObjectIdentifier    pkcs_12PbeIds  = pkcs_12.branch("1");

    static final ASN1ObjectIdentifier    pbeWithSHAAnd128BitRC4 = pkcs_12PbeIds.branch("1");
    static final ASN1ObjectIdentifier    pbeWithSHAAnd40BitRC4  = pkcs_12PbeIds.branch("2");
    static final ASN1ObjectIdentifier    pbeWithSHAAnd3_KeyTripleDES_CBC = pkcs_12PbeIds.branch("3");
    static final ASN1ObjectIdentifier    pbeWithSHAAnd2_KeyTripleDES_CBC = pkcs_12PbeIds.branch("4");
    static final ASN1ObjectIdentifier    pbeWithSHAAnd128BitRC2_CBC = pkcs_12PbeIds.branch("5");
    static final ASN1ObjectIdentifier    pbewithSHAAnd40BitRC2_CBC = pkcs_12PbeIds.branch("6");

    static final ASN1ObjectIdentifier    id_alg_CMS3DESwrap = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.6");
    static final ASN1ObjectIdentifier    id_alg_CMSRC2wrap = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.3.7");
}

