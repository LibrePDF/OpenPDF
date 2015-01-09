package old.org.bouncycastle.jce.provider.asymmetric;

import java.util.HashMap;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.eac.EACObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

public class EC
{
    public static class Mappings
        extends HashMap
    {
        public Mappings()
        {
            put("KeyAgreement.ECDH", "org.bouncycastle.jce.provider.asymmetric.ec.KeyAgreement$DH");
            put("KeyAgreement.ECDHC", "org.bouncycastle.jce.provider.asymmetric.ec.KeyAgreement$DHC");
            put("KeyAgreement.ECMQV", "org.bouncycastle.jce.provider.asymmetric.ec.KeyAgreement$MQV");
            put("KeyAgreement." + X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme, "org.bouncycastle.jce.provider.asymmetric.ec.KeyAgreement$DHwithSHA1KDF");
            put("KeyAgreement." + X9ObjectIdentifiers.mqvSinglePass_sha1kdf_scheme, "org.bouncycastle.jce.provider.asymmetric.ec.KeyAgreement$MQVwithSHA1KDF");

            put("KeyFactory.EC", "org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory$EC");
            put("KeyFactory.ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory$ECDSA");
            put("KeyFactory.ECDH", "org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory$ECDH");
            put("KeyFactory.ECDHC", "org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory$ECDHC");
            put("KeyFactory.ECMQV", "org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory$ECMQV");
            put("Alg.Alias.KeyFactory." + X9ObjectIdentifiers.id_ecPublicKey, "EC");
            // TODO Should this be an alias for ECDH?
            put("Alg.Alias.KeyFactory." + X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme, "EC");
            put("Alg.Alias.KeyFactory." + X9ObjectIdentifiers.mqvSinglePass_sha1kdf_scheme, "ECMQV");

            put("KeyFactory.ECGOST3410", "org.bouncycastle.jce.provider.asymmetric.ec.KeyFactory$ECGOST3410");
            put("Alg.Alias.KeyFactory.GOST-3410-2001", "ECGOST3410");
            put("Alg.Alias.KeyFactory.ECGOST-3410", "ECGOST3410");
            put("Alg.Alias.KeyFactory." + CryptoProObjectIdentifiers.gostR3410_2001, "ECGOST3410");

            put("KeyPairGenerator.EC", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$EC");
            put("KeyPairGenerator.ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$ECDSA");
            put("KeyPairGenerator.ECDH", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$ECDH");
            put("KeyPairGenerator.ECDHC", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$ECDHC");
            put("KeyPairGenerator.ECIES", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$ECDH");
            put("KeyPairGenerator.ECMQV", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$ECMQV");
            // TODO Should this be an alias for ECDH?
            put("Alg.Alias.KeyPairGenerator." + X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme, "EC");
            put("Alg.Alias.KeyPairGenerator." + X9ObjectIdentifiers.mqvSinglePass_sha1kdf_scheme, "ECMQV");

            put("KeyPairGenerator.ECGOST3410", "org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator$ECGOST3410");
            put("Alg.Alias.KeyPairGenerator.ECGOST-3410", "ECGOST3410");
            put("Alg.Alias.KeyPairGenerator.GOST-3410-2001", "ECGOST3410");

            put("Signature.ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSA");
            put("Signature.NONEwithECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSAnone");

            put("Alg.Alias.Signature.SHA1withECDSA", "ECDSA");
            put("Alg.Alias.Signature.ECDSAwithSHA1", "ECDSA");
            put("Alg.Alias.Signature.SHA1WITHECDSA", "ECDSA");
            put("Alg.Alias.Signature.ECDSAWITHSHA1", "ECDSA");
            put("Alg.Alias.Signature.SHA1WithECDSA", "ECDSA");
            put("Alg.Alias.Signature.ECDSAWithSHA1", "ECDSA");
            put("Alg.Alias.Signature.1.2.840.10045.4.1", "ECDSA");
            put("Alg.Alias.Signature." + TeleTrusTObjectIdentifiers.ecSignWithSha1, "ECDSA");

            addSignatureAlgorithm("SHA224", "ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSA224", X9ObjectIdentifiers.ecdsa_with_SHA224);
            addSignatureAlgorithm("SHA256", "ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSA256", X9ObjectIdentifiers.ecdsa_with_SHA256);
            addSignatureAlgorithm("SHA384", "ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSA384", X9ObjectIdentifiers.ecdsa_with_SHA384);
            addSignatureAlgorithm("SHA512", "ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSA512", X9ObjectIdentifiers.ecdsa_with_SHA512);
            addSignatureAlgorithm("RIPEMD160", "ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecDSARipeMD160",TeleTrusTObjectIdentifiers.ecSignWithRipemd160);

            put("Signature.SHA1WITHECNR", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecNR");
            put("Signature.SHA224WITHECNR", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecNR224");
            put("Signature.SHA256WITHECNR", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecNR256");
            put("Signature.SHA384WITHECNR", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecNR384");
            put("Signature.SHA512WITHECNR", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecNR512");

            addSignatureAlgorithm("SHA1", "CVC-ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecCVCDSA", EACObjectIdentifiers.id_TA_ECDSA_SHA_1);
            addSignatureAlgorithm("SHA224", "CVC-ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecCVCDSA224", EACObjectIdentifiers.id_TA_ECDSA_SHA_224);
            addSignatureAlgorithm("SHA256", "CVC-ECDSA", "org.bouncycastle.jce.provider.asymmetric.ec.Signature$ecCVCDSA256", EACObjectIdentifiers.id_TA_ECDSA_SHA_256);
        }

        private void addSignatureAlgorithm(
            String digest,
            String algorithm,
            String className,
            DERObjectIdentifier oid)
        {
            String mainName = digest + "WITH" + algorithm;
            String jdk11Variation1 = digest + "with" + algorithm;
            String jdk11Variation2 = digest + "With" + algorithm;
            String alias = digest + "/" + algorithm;

            put("Signature." + mainName, className);
            put("Alg.Alias.Signature." + jdk11Variation1, mainName);
            put("Alg.Alias.Signature." + jdk11Variation2, mainName);
            put("Alg.Alias.Signature." + alias, mainName);
            put("Alg.Alias.Signature." + oid, mainName);
            put("Alg.Alias.Signature.OID." + oid, mainName);
        }
    }
}
