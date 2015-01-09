package old.org.bouncycastle.asn1.teletrust;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface TeleTrusTObjectIdentifiers
{
    static final ASN1ObjectIdentifier teleTrusTAlgorithm = new ASN1ObjectIdentifier("1.3.36.3");

    static final ASN1ObjectIdentifier    ripemd160           = teleTrusTAlgorithm.branch("2.1");
    static final ASN1ObjectIdentifier    ripemd128           = teleTrusTAlgorithm.branch("2.2");
    static final ASN1ObjectIdentifier    ripemd256           = teleTrusTAlgorithm.branch("2.3");

    static final ASN1ObjectIdentifier teleTrusTRSAsignatureAlgorithm = teleTrusTAlgorithm.branch("3.1");

    static final ASN1ObjectIdentifier    rsaSignatureWithripemd160           = teleTrusTRSAsignatureAlgorithm.branch("2");
    static final ASN1ObjectIdentifier    rsaSignatureWithripemd128           = teleTrusTRSAsignatureAlgorithm.branch("3");
    static final ASN1ObjectIdentifier    rsaSignatureWithripemd256           = teleTrusTRSAsignatureAlgorithm.branch("4");

    static final ASN1ObjectIdentifier    ecSign = teleTrusTAlgorithm.branch("3.2");

    static final ASN1ObjectIdentifier    ecSignWithSha1  = ecSign.branch("1");
    static final ASN1ObjectIdentifier    ecSignWithRipemd160  = ecSign.branch("2");

    static final ASN1ObjectIdentifier ecc_brainpool = teleTrusTAlgorithm.branch("3.2.8");
    static final ASN1ObjectIdentifier ellipticCurve = ecc_brainpool.branch("1");
    static final ASN1ObjectIdentifier versionOne = ellipticCurve.branch("1");

    static final ASN1ObjectIdentifier brainpoolP160r1 = versionOne.branch("1");
    static final ASN1ObjectIdentifier brainpoolP160t1 = versionOne.branch("2");
    static final ASN1ObjectIdentifier brainpoolP192r1 = versionOne.branch("3");
    static final ASN1ObjectIdentifier brainpoolP192t1 = versionOne.branch("4");
    static final ASN1ObjectIdentifier brainpoolP224r1 = versionOne.branch("5");
    static final ASN1ObjectIdentifier brainpoolP224t1 = versionOne.branch("6");
    static final ASN1ObjectIdentifier brainpoolP256r1 = versionOne.branch("7");
    static final ASN1ObjectIdentifier brainpoolP256t1 = versionOne.branch("8");
    static final ASN1ObjectIdentifier brainpoolP320r1 = versionOne.branch("9");
    static final ASN1ObjectIdentifier brainpoolP320t1 = versionOne.branch("10");
    static final ASN1ObjectIdentifier brainpoolP384r1 = versionOne.branch("11");
    static final ASN1ObjectIdentifier brainpoolP384t1 = versionOne.branch("12");
    static final ASN1ObjectIdentifier brainpoolP512r1 = versionOne.branch("13");
    static final ASN1ObjectIdentifier brainpoolP512t1 = versionOne.branch("14");
}
