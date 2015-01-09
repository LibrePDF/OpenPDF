package old.org.bouncycastle.jce.provider;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import old.org.bouncycastle.crypto.params.RSAKeyParameters;
import old.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

/**
 * utility class for converting java.security RSA objects into their
 * org.bouncycastle.crypto counterparts.
 */
class RSAUtil
{
    static boolean isRsaOid(
        DERObjectIdentifier algOid)
    {
        return algOid.equals(PKCSObjectIdentifiers.rsaEncryption)
            || algOid.equals(X509ObjectIdentifiers.id_ea_rsa)
            || algOid.equals(PKCSObjectIdentifiers.id_RSASSA_PSS)
            || algOid.equals(PKCSObjectIdentifiers.id_RSAES_OAEP);
    }
    
    static RSAKeyParameters generatePublicKeyParameter(
        RSAPublicKey    key)
    {
        return new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());

    }

    static RSAKeyParameters generatePrivateKeyParameter(
        RSAPrivateKey    key)
    {
        if (key instanceof RSAPrivateCrtKey)
        {
            RSAPrivateCrtKey    k = (RSAPrivateCrtKey)key;

            return new RSAPrivateCrtKeyParameters(k.getModulus(),
                k.getPublicExponent(), k.getPrivateExponent(),
                k.getPrimeP(), k.getPrimeQ(), k.getPrimeExponentP(),                            k.getPrimeExponentQ(), k.getCrtCoefficient());
        }
        else
        {
            RSAPrivateKey    k = key;

            return new RSAKeyParameters(true, k.getModulus(), k.getPrivateExponent());
        }
    }
}
