package old.org.bouncycastle.jce;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x9.X962Parameters;
import old.org.bouncycastle.asn1.x9.X9ECParameters;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.jce.provider.ProviderUtil;
import old.org.bouncycastle.jce.provider.asymmetric.ec.ECUtil;

/**
 * Utility class to allow conversion of EC key parameters to explicit from named
 * curves and back (where possible).
 */
public class ECKeyUtil
{
    /**
     * Convert a passed in public EC key to have explicit parameters. If the key
     * is already using explicit parameters it is returned.
     *
     * @param key key to be converted
     * @param providerName provider name to be used.
     * @return the equivalent key with explicit curve parameters
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static PublicKey publicToExplicitParameters(PublicKey key, String providerName)
        throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException
    {
        Provider provider = Security.getProvider(providerName);

        if (provider == null)
        {
            throw new NoSuchProviderException("cannot find provider: " + providerName);
        }

        return publicToExplicitParameters(key, provider);
    }

    /**
     * Convert a passed in public EC key to have explicit parameters. If the key
     * is already using explicit parameters it is returned.
     *
     * @param key key to be converted
     * @param provider provider to be used.
     * @return the equivalent key with explicit curve parameters
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey publicToExplicitParameters(PublicKey key, Provider provider)
        throws IllegalArgumentException, NoSuchAlgorithmException
    {
        try
        {
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(ASN1Object.fromByteArray(key.getEncoded()));

            if (info.getAlgorithmId().getObjectId().equals(CryptoProObjectIdentifiers.gostR3410_2001))
            {
                throw new IllegalArgumentException("cannot convert GOST key to explicit parameters.");
            }
            else
            {
                X962Parameters params = new X962Parameters((DERObject)info.getAlgorithmId().getParameters());
                X9ECParameters curveParams;

                if (params.isNamedCurve())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)params.getParameters();

                    curveParams = ECUtil.getNamedCurveByOid(oid);
                    // ignore seed value due to JDK bug
                    curveParams = new X9ECParameters(curveParams.getCurve(), curveParams.getG(), curveParams.getN(), curveParams.getH());
                }
                else if (params.isImplicitlyCA())
                {
                    curveParams = new X9ECParameters(ProviderUtil.getEcImplicitlyCa().getCurve(), ProviderUtil.getEcImplicitlyCa().getG(), ProviderUtil.getEcImplicitlyCa().getN(), ProviderUtil.getEcImplicitlyCa().getH());
                }
                else
                {
                    return key;   // already explicit
                }

                params = new X962Parameters(curveParams);

                info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params.getDERObject()), info.getPublicKeyData().getBytes());

                KeyFactory keyFact = KeyFactory.getInstance(key.getAlgorithm(), provider);

                return keyFact.generatePublic(new X509EncodedKeySpec(info.getEncoded()));
            }
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw e;
        }
        catch (Exception e)
        {               // shouldn't really happen...
            throw new UnexpectedException(e);
        }
    }

    /**
     * Convert a passed in private EC key to have explicit parameters. If the key
     * is already using explicit parameters it is returned.
     *
     * @param key key to be converted
     * @param providerName provider name to be used.
     * @return the equivalent key with explicit curve parameters
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static PrivateKey privateToExplicitParameters(PrivateKey key, String providerName)
        throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException
    {
        Provider provider = Security.getProvider(providerName);

        if (provider == null)
        {
            throw new NoSuchProviderException("cannot find provider: " + providerName);
        }

        return privateToExplicitParameters(key, provider);
    }

    /**
     * Convert a passed in private EC key to have explicit parameters. If the key
     * is already using explicit parameters it is returned.
     *
     * @param key key to be converted
     * @param provider provider to be used.
     * @return the equivalent key with explicit curve parameters
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey privateToExplicitParameters(PrivateKey key, Provider provider)
        throws IllegalArgumentException, NoSuchAlgorithmException
    {
        try
        {
            PrivateKeyInfo info = PrivateKeyInfo.getInstance(ASN1Object.fromByteArray(key.getEncoded()));

            if (info.getAlgorithmId().getObjectId().equals(CryptoProObjectIdentifiers.gostR3410_2001))
            {
                throw new UnsupportedEncodingException("cannot convert GOST key to explicit parameters.");
            }
            else
            {
                X962Parameters params = new X962Parameters((DERObject)info.getAlgorithmId().getParameters());
                X9ECParameters curveParams;

                if (params.isNamedCurve())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)params.getParameters();

                    curveParams = ECUtil.getNamedCurveByOid(oid);
                    // ignore seed value due to JDK bug
                    curveParams = new X9ECParameters(curveParams.getCurve(), curveParams.getG(), curveParams.getN(), curveParams.getH());
                }
                else if (params.isImplicitlyCA())
                {
                    curveParams = new X9ECParameters(ProviderUtil.getEcImplicitlyCa().getCurve(), ProviderUtil.getEcImplicitlyCa().getG(), ProviderUtil.getEcImplicitlyCa().getN(), ProviderUtil.getEcImplicitlyCa().getH());
                }
                else
                {
                    return key;   // already explicit
                }

                params = new X962Parameters(curveParams);

                info = new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params.getDERObject()), info.getPrivateKey());

                KeyFactory keyFact = KeyFactory.getInstance(key.getAlgorithm(), provider);

                return keyFact.generatePrivate(new PKCS8EncodedKeySpec(info.getEncoded()));
            }
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw e;
        }
        catch (Exception e)
        {          // shouldn't really happen
            throw new UnexpectedException(e);
        }
    }

    private static class UnexpectedException
        extends RuntimeException
    {
        private Throwable cause;

        UnexpectedException(Throwable cause)
        {
            super(cause.toString());

            this.cause = cause;
        }

        public Throwable getCause()
        {
            return cause;
        }
    }
}
