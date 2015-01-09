package old.org.bouncycastle.asn1.cmp;

import old.org.bouncycastle.asn1.DERObjectIdentifier;

public interface CMPObjectIdentifiers
{
    // RFC 4210

    // id-PasswordBasedMac OBJECT IDENTIFIER ::= {1 2 840 113533 7 66 13}
    static final DERObjectIdentifier    passwordBasedMac        = new DERObjectIdentifier("1.2.840.113533.7.66.13");

    // id-DHBasedMac OBJECT IDENTIFIER ::= {1 2 840 113533 7 66 30}
    static final DERObjectIdentifier    dhBasedMac              = new DERObjectIdentifier("1.2.840.113533.7.66.30");

    // Example InfoTypeAndValue contents include, but are not limited
    // to, the following (un-comment in this ASN.1 module and use as
    // appropriate for a given environment):
    //
    //   id-it-caProtEncCert    OBJECT IDENTIFIER ::= {id-it 1}
    //      CAProtEncCertValue      ::= CMPCertificate
    //   id-it-signKeyPairTypes OBJECT IDENTIFIER ::= {id-it 2}
    //      SignKeyPairTypesValue   ::= SEQUENCE OF AlgorithmIdentifier
    //   id-it-encKeyPairTypes  OBJECT IDENTIFIER ::= {id-it 3}
    //      EncKeyPairTypesValue    ::= SEQUENCE OF AlgorithmIdentifier
    //   id-it-preferredSymmAlg OBJECT IDENTIFIER ::= {id-it 4}
    //      PreferredSymmAlgValue   ::= AlgorithmIdentifier
    //   id-it-caKeyUpdateInfo  OBJECT IDENTIFIER ::= {id-it 5}
    //      CAKeyUpdateInfoValue    ::= CAKeyUpdAnnContent
    //   id-it-currentCRL       OBJECT IDENTIFIER ::= {id-it 6}
    //      CurrentCRLValue         ::= CertificateList
    //   id-it-unsupportedOIDs  OBJECT IDENTIFIER ::= {id-it 7}
    //      UnsupportedOIDsValue    ::= SEQUENCE OF OBJECT IDENTIFIER
    //   id-it-keyPairParamReq  OBJECT IDENTIFIER ::= {id-it 10}
    //      KeyPairParamReqValue    ::= OBJECT IDENTIFIER
    //   id-it-keyPairParamRep  OBJECT IDENTIFIER ::= {id-it 11}
    //      KeyPairParamRepValue    ::= AlgorithmIdentifer
    //   id-it-revPassphrase    OBJECT IDENTIFIER ::= {id-it 12}
    //      RevPassphraseValue      ::= EncryptedValue
    //   id-it-implicitConfirm  OBJECT IDENTIFIER ::= {id-it 13}
    //      ImplicitConfirmValue    ::= NULL
    //   id-it-confirmWaitTime  OBJECT IDENTIFIER ::= {id-it 14}
    //      ConfirmWaitTimeValue    ::= GeneralizedTime
    //   id-it-origPKIMessage   OBJECT IDENTIFIER ::= {id-it 15}
    //      OrigPKIMessageValue     ::= PKIMessages
    //   id-it-suppLangTags     OBJECT IDENTIFIER ::= {id-it 16}
    //      SuppLangTagsValue       ::= SEQUENCE OF UTF8String
    //
    // where
    //
    //   id-pkix OBJECT IDENTIFIER ::= {
    //      iso(1) identified-organization(3)
    //      dod(6) internet(1) security(5) mechanisms(5) pkix(7)}
    // and
    //   id-it   OBJECT IDENTIFIER ::= {id-pkix 4}
    static final DERObjectIdentifier    it_caProtEncCert        = new DERObjectIdentifier("1.3.6.1.5.5.7.4.1");
    static final DERObjectIdentifier    it_signKeyPairTypes     = new DERObjectIdentifier("1.3.6.1.5.5.7.4.2");
    static final DERObjectIdentifier    it_encKeyPairTypes      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.3");
    static final DERObjectIdentifier    it_preferredSymAlg      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.4");
    static final DERObjectIdentifier    it_caKeyUpdateInfo      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.5");
    static final DERObjectIdentifier    it_currentCRL           = new DERObjectIdentifier("1.3.6.1.5.5.7.4.6");
    static final DERObjectIdentifier    it_unsupportedOIDs      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.7");
    static final DERObjectIdentifier    it_keyPairParamReq      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.10");
    static final DERObjectIdentifier    it_keyPairParamRep      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.11");
    static final DERObjectIdentifier    it_revPassphrase        = new DERObjectIdentifier("1.3.6.1.5.5.7.4.12");
    static final DERObjectIdentifier    it_implicitConfirm      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.13");
    static final DERObjectIdentifier    it_confirmWaitTime      = new DERObjectIdentifier("1.3.6.1.5.5.7.4.14");
    static final DERObjectIdentifier    it_origPKIMessage       = new DERObjectIdentifier("1.3.6.1.5.5.7.4.15");
    static final DERObjectIdentifier    it_suppLangTags         = new DERObjectIdentifier("1.3.6.1.5.5.7.4.16");

    // RFC 4211

    // id-pkix  OBJECT IDENTIFIER  ::= { iso(1) identified-organization(3)
    //     dod(6) internet(1) security(5) mechanisms(5) pkix(7) }
    //
    // arc for Internet X.509 PKI protocols and their components
    // id-pkip  OBJECT IDENTIFIER :: { id-pkix pkip(5) }
    //
    // arc for Registration Controls in CRMF
    // id-regCtrl  OBJECT IDENTIFIER ::= { id-pkip regCtrl(1) }
    //
    // arc for Registration Info in CRMF
    // id-regInfo       OBJECT IDENTIFIER ::= { id-pkip id-regInfo(2) }

    static final DERObjectIdentifier    regCtrl_regToken        = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.1");
    static final DERObjectIdentifier    regCtrl_authenticator   = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.2");
    static final DERObjectIdentifier    regCtrl_pkiPublicationInfo = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.3");
    static final DERObjectIdentifier    regCtrl_pkiArchiveOptions  = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.4");
    static final DERObjectIdentifier    regCtrl_oldCertID       = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.5");
    static final DERObjectIdentifier    regCtrl_protocolEncrKey = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.6");

    // From RFC4210:
    // id-regCtrl-altCertTemplate OBJECT IDENTIFIER ::= {id-regCtrl 7}
    static final DERObjectIdentifier    regCtrl_altCertTemplate = new DERObjectIdentifier("1.3.6.1.5.5.7.5.1.7");

    static final DERObjectIdentifier    regInfo_utf8Pairs       = new DERObjectIdentifier("1.3.6.1.5.5.7.5.2.1");
    static final DERObjectIdentifier    regInfo_certReq         = new DERObjectIdentifier("1.3.6.1.5.5.7.5.2.2");

    // id-smime OBJECT IDENTIFIER ::= { iso(1) member-body(2)
    //         us(840) rsadsi(113549) pkcs(1) pkcs9(9) 16 }
    //
    // id-ct   OBJECT IDENTIFIER ::= { id-smime  1 }  -- content types
    //
    // id-ct-encKeyWithID OBJECT IDENTIFIER ::= {id-ct 21}
    static final DERObjectIdentifier    ct_encKeyWithID         = new DERObjectIdentifier("1.2.840.113549.1.9.16.1.21");

}
