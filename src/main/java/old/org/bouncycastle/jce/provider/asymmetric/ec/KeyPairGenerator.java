package old.org.bouncycastle.jce.provider.asymmetric.ec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.util.Hashtable;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import old.org.bouncycastle.asn1.nist.NISTNamedCurves;
import old.org.bouncycastle.asn1.sec.SECNamedCurves;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;
import old.org.bouncycastle.asn1.x9.X962NamedCurves;
import old.org.bouncycastle.asn1.x9.X9ECParameters;
import old.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import old.org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import old.org.bouncycastle.crypto.params.ECDomainParameters;
import old.org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import old.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import old.org.bouncycastle.jce.provider.JCEECPrivateKey;
import old.org.bouncycastle.jce.provider.JCEECPublicKey;
import old.org.bouncycastle.jce.provider.JDKKeyPairGenerator;
import old.org.bouncycastle.jce.provider.ProviderUtil;
import old.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import old.org.bouncycastle.jce.spec.ECParameterSpec;
import old.org.bouncycastle.math.ec.ECCurve;
import old.org.bouncycastle.math.ec.ECPoint;

public abstract class KeyPairGenerator
    extends JDKKeyPairGenerator
{
    public KeyPairGenerator(String algorithmName)
    {
        super(algorithmName);
    }

    public static class EC
        extends KeyPairGenerator
    {
        ECKeyGenerationParameters   param;
        ECKeyPairGenerator          engine = new ECKeyPairGenerator();
        Object                      ecParams = null;
        int                         strength = 239;
        int                         certainty = 50;
        SecureRandom                random = new SecureRandom();
        boolean                     initialised = false;
        String                      algorithm;

        static private Hashtable    ecParameters;

        static {
            ecParameters = new Hashtable();

            ecParameters.put(new Integer(192), new ECGenParameterSpec("prime192v1")); // a.k.a P-192
            ecParameters.put(new Integer(239), new ECGenParameterSpec("prime239v1"));
            ecParameters.put(new Integer(256), new ECGenParameterSpec("prime256v1")); // a.k.a P-256

            ecParameters.put(new Integer(224), new ECGenParameterSpec("P-224"));
            ecParameters.put(new Integer(384), new ECGenParameterSpec("P-384"));
            ecParameters.put(new Integer(521), new ECGenParameterSpec("P-521"));
        }

        public EC()
        {
            super("EC");
            this.algorithm = "EC";
        }

        public EC(
            String  algorithm)
        {
            super(algorithm);
            this.algorithm = algorithm;
        }

        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            this.strength = strength;
            this.random = random;
            this.ecParams = ecParameters.get(new Integer(strength));

            if (ecParams != null)
            {
                try
                {
                    initialize((ECGenParameterSpec)ecParams, random);
                }
                catch (InvalidAlgorithmParameterException e)
                {
                    throw new InvalidParameterException("key size not configurable.");
                }
            }
            else
            {
                throw new InvalidParameterException("unknown key size.");
            }
        }

        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (params instanceof ECParameterSpec)
            {
                ECParameterSpec p = (ECParameterSpec)params;
                this.ecParams = params;

                param = new ECKeyGenerationParameters(new ECDomainParameters(p.getCurve(), p.getG(), p.getN()), random);

                engine.init(param);
                initialised = true;
            }
            else if (params instanceof java.security.spec.ECParameterSpec)
            {
                java.security.spec.ECParameterSpec p = (java.security.spec.ECParameterSpec)params;
                this.ecParams = params;

                ECCurve curve = EC5Util.convertCurve(p.getCurve());
                ECPoint g = EC5Util.convertPoint(curve, p.getGenerator(), false);

                param = new ECKeyGenerationParameters(new ECDomainParameters(curve, g, p.getOrder(), BigInteger.valueOf(p.getCofactor())), random);

                engine.init(param);
                initialised = true;
            }
            else if (params instanceof ECGenParameterSpec)
            {
                final String curveName = ((ECGenParameterSpec)params).getName();

                if (this.algorithm.equals("ECGOST3410"))
                {
                    ECDomainParameters  ecP = ECGOST3410NamedCurves.getByName(curveName);
                    if (ecP == null)
                    {
                        throw new InvalidAlgorithmParameterException("unknown curve name: " + curveName);
                    }

                    this.ecParams = new ECNamedCurveSpec(
                                                    curveName,
                                                    ecP.getCurve(),
                                                    ecP.getG(),
                                                    ecP.getN(),
                                                    ecP.getH(),
                                                    ecP.getSeed());
                }
                else
                {
                    X9ECParameters  ecP = X962NamedCurves.getByName(curveName);
                    if (ecP == null)
                    {
                        ecP = SECNamedCurves.getByName(curveName);
                        if (ecP == null)
                        {
                            ecP = NISTNamedCurves.getByName(curveName);
                        }
                        if (ecP == null)
                        {
                            ecP = TeleTrusTNamedCurves.getByName(curveName);
                        }
                        if (ecP == null)
                        {
                            // See if it's actually an OID string (SunJSSE ServerHandshaker setupEphemeralECDHKeys bug)
                            try
                            {
                                DERObjectIdentifier oid = new DERObjectIdentifier(curveName);
                                ecP = X962NamedCurves.getByOID(oid);
                                if (ecP == null)
                                {
                                    ecP = SECNamedCurves.getByOID(oid);
                                }
                                if (ecP == null)
                                {
                                    ecP = NISTNamedCurves.getByOID(oid);
                                }
                                if (ecP == null)
                                {
                                    ecP = TeleTrusTNamedCurves.getByOID(oid);
                                }
                                if (ecP == null)
                                {
                                    throw new InvalidAlgorithmParameterException("unknown curve OID: " + curveName);
                                }
                            }
                            catch (IllegalArgumentException ex)
                            {
                                throw new InvalidAlgorithmParameterException("unknown curve name: " + curveName);
                            }
                        }
                    }

                    this.ecParams = new ECNamedCurveSpec(
                            curveName,
                            ecP.getCurve(),
                            ecP.getG(),
                            ecP.getN(),
                            ecP.getH(),
                            null); // ecP.getSeed());   Work-around JDK bug -- it won't look up named curves properly if seed is present 
                }

                java.security.spec.ECParameterSpec p = (java.security.spec.ECParameterSpec)ecParams;

                ECCurve curve = EC5Util.convertCurve(p.getCurve());
                ECPoint g = EC5Util.convertPoint(curve, p.getGenerator(), false);

                param = new ECKeyGenerationParameters(new ECDomainParameters(curve, g, p.getOrder(), BigInteger.valueOf(p.getCofactor())), random);

                engine.init(param);
                initialised = true;
            }
            else if (params == null && ProviderUtil.getEcImplicitlyCa() != null)
            {
                ECParameterSpec p = ProviderUtil.getEcImplicitlyCa();
                this.ecParams = params;

                param = new ECKeyGenerationParameters(new ECDomainParameters(p.getCurve(), p.getG(), p.getN()), random);

                engine.init(param);
                initialised = true;
            }
            else if (params == null && ProviderUtil.getEcImplicitlyCa() == null)
            {
                throw new InvalidAlgorithmParameterException("null parameter passed but no implicitCA set");
            }
            else
            {
                throw new InvalidAlgorithmParameterException("parameter object not a ECParameterSpec");
            }
        }

        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                throw new IllegalStateException("EC Key Pair Generator not initialised");
            }

            AsymmetricCipherKeyPair     pair = engine.generateKeyPair();
            ECPublicKeyParameters       pub = (ECPublicKeyParameters)pair.getPublic();
            ECPrivateKeyParameters      priv = (ECPrivateKeyParameters)pair.getPrivate();

            if (ecParams instanceof ECParameterSpec)
            {
                ECParameterSpec p = (ECParameterSpec)ecParams;

                JCEECPublicKey pubKey = new JCEECPublicKey(algorithm, pub, p);
                return new KeyPair(pubKey,
                                   new JCEECPrivateKey(algorithm, priv, pubKey, p));
            }
            else if (ecParams == null)
            {
               return new KeyPair(new JCEECPublicKey(algorithm, pub),
                                   new JCEECPrivateKey(algorithm, priv));
            }
            else
            {
                java.security.spec.ECParameterSpec p = (java.security.spec.ECParameterSpec)ecParams;

                JCEECPublicKey pubKey = new JCEECPublicKey(algorithm, pub, p);
                
                return new KeyPair(pubKey, new JCEECPrivateKey(algorithm, priv, pubKey, p));
            }
        }
    }

    public static class ECDSA
        extends EC
    {
        public ECDSA()
        {
            super("ECDSA");
        }
    }

    public static class ECGOST3410
        extends EC
    {
        public ECGOST3410()
        {
            super("ECGOST3410");
        }
    }

    public static class ECDH
        extends EC
    {
        public ECDH()
        {
            super("ECDH");
        }
    }

    public static class ECDHC
        extends EC
    {
        public ECDHC()
        {
            super("ECDHC");
        }
    }

    public static class ECMQV
        extends EC
    {
        public ECMQV()
        {
            super("ECMQV");
        }
    }
}