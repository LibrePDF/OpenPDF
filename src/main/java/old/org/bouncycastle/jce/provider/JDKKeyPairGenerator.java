package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import old.org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;
import old.org.bouncycastle.crypto.generators.DHParametersGenerator;
import old.org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import old.org.bouncycastle.crypto.generators.DSAParametersGenerator;
import old.org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;
import old.org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
import old.org.bouncycastle.crypto.generators.GOST3410KeyPairGenerator;
import old.org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import old.org.bouncycastle.crypto.params.DHKeyGenerationParameters;
import old.org.bouncycastle.crypto.params.DHParameters;
import old.org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import old.org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import old.org.bouncycastle.crypto.params.DSAParameters;
import old.org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import old.org.bouncycastle.crypto.params.ElGamalKeyGenerationParameters;
import old.org.bouncycastle.crypto.params.ElGamalParameters;
import old.org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import old.org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import old.org.bouncycastle.crypto.params.GOST3410KeyGenerationParameters;
import old.org.bouncycastle.crypto.params.GOST3410Parameters;
import old.org.bouncycastle.crypto.params.GOST3410PrivateKeyParameters;
import old.org.bouncycastle.crypto.params.GOST3410PublicKeyParameters;
import old.org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import old.org.bouncycastle.crypto.params.RSAKeyParameters;
import old.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import old.org.bouncycastle.jce.spec.ElGamalParameterSpec;
import old.org.bouncycastle.jce.spec.GOST3410ParameterSpec;
import old.org.bouncycastle.jce.spec.GOST3410PublicKeyParameterSetSpec;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Hashtable;

import javax.crypto.spec.DHParameterSpec;

public abstract class JDKKeyPairGenerator
    extends KeyPairGenerator
{
    public JDKKeyPairGenerator(
        String              algorithmName)
    {
        super(algorithmName);
    }

    public abstract void initialize(int strength, SecureRandom random);

    public abstract KeyPair generateKeyPair();

    public static class RSA
        extends JDKKeyPairGenerator
    {
        final static BigInteger defaultPublicExponent = BigInteger.valueOf(0x10001);
        final static int defaultTests = 12;

        RSAKeyGenerationParameters  param;
        RSAKeyPairGenerator         engine;

        public RSA()
        {
            super("RSA");

            engine = new RSAKeyPairGenerator();
            param = new RSAKeyGenerationParameters(defaultPublicExponent,
                            new SecureRandom(), 2048, defaultTests);
            engine.init(param);
        }

        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            param = new RSAKeyGenerationParameters(defaultPublicExponent,
                            random, strength, defaultTests);

            engine.init(param);
        }

        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof RSAKeyGenParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a RSAKeyGenParameterSpec");
            }
            RSAKeyGenParameterSpec     rsaParams = (RSAKeyGenParameterSpec)params;

            param = new RSAKeyGenerationParameters(
                            rsaParams.getPublicExponent(),
                            random, rsaParams.getKeysize(), defaultTests);

            engine.init(param);
        }

        public KeyPair generateKeyPair()
        {
            AsymmetricCipherKeyPair     pair = engine.generateKeyPair();
            RSAKeyParameters            pub = (RSAKeyParameters)pair.getPublic();
            RSAPrivateCrtKeyParameters  priv = (RSAPrivateCrtKeyParameters)pair.getPrivate();

            return new KeyPair(new JCERSAPublicKey(pub),
                               new JCERSAPrivateCrtKey(priv));
        }
    }

    public static class DH
        extends JDKKeyPairGenerator
    {
        private static Hashtable   params = new Hashtable();

        DHKeyGenerationParameters  param;
        DHBasicKeyPairGenerator    engine = new DHBasicKeyPairGenerator();
        int                        strength = 1024;
        int                        certainty = 20;
        SecureRandom               random = new SecureRandom();
        boolean                    initialised = false;

        public DH()
        {
            super("DH");
        }

        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            this.strength = strength;
            this.random = random;
        }

        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof DHParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec");
            }
            DHParameterSpec     dhParams = (DHParameterSpec)params;

            param = new DHKeyGenerationParameters(random, new DHParameters(dhParams.getP(), dhParams.getG(), null, dhParams.getL()));

            engine.init(param);
            initialised = true;
        }

        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                Integer paramStrength = new Integer(strength);

                if (params.containsKey(paramStrength))
                {
                    param = (DHKeyGenerationParameters)params.get(paramStrength);
                }
                else
                {
                    DHParametersGenerator   pGen = new DHParametersGenerator();

                    pGen.init(strength, certainty, random);

                    param = new DHKeyGenerationParameters(random, pGen.generateParameters());

                    params.put(paramStrength, param);
                }

                engine.init(param);

                initialised = true;
            }

            AsymmetricCipherKeyPair pair = engine.generateKeyPair();
            DHPublicKeyParameters   pub = (DHPublicKeyParameters)pair.getPublic();
            DHPrivateKeyParameters  priv = (DHPrivateKeyParameters)pair.getPrivate();

            return new KeyPair(new JCEDHPublicKey(pub),
                               new JCEDHPrivateKey(priv));
        }
    }

    public static class DSA
        extends JDKKeyPairGenerator
    {
        DSAKeyGenerationParameters param;
        DSAKeyPairGenerator        engine = new DSAKeyPairGenerator();
        int                        strength = 1024;
        int                        certainty = 20;
        SecureRandom               random = new SecureRandom();
        boolean                    initialised = false;

        public DSA()
        {
            super("DSA");
        }

        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            if (strength < 512 || strength > 1024 || strength % 64 != 0)
            {
                throw new InvalidParameterException("strength must be from 512 - 1024 and a multiple of 64");
            }

            this.strength = strength;
            this.random = random;
        }

        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof DSAParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a DSAParameterSpec");
            }
            DSAParameterSpec     dsaParams = (DSAParameterSpec)params;

            param = new DSAKeyGenerationParameters(random, new DSAParameters(dsaParams.getP(), dsaParams.getQ(), dsaParams.getG()));

            engine.init(param);
            initialised = true;
        }

        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                DSAParametersGenerator   pGen = new DSAParametersGenerator();

                pGen.init(strength, certainty, random);
                param = new DSAKeyGenerationParameters(random, pGen.generateParameters());
                engine.init(param);
                initialised = true;
            }

            AsymmetricCipherKeyPair   pair = engine.generateKeyPair();
            DSAPublicKeyParameters     pub = (DSAPublicKeyParameters)pair.getPublic();
            DSAPrivateKeyParameters priv = (DSAPrivateKeyParameters)pair.getPrivate();

            return new KeyPair(new JDKDSAPublicKey(pub),
                               new JDKDSAPrivateKey(priv));
        }
    }

    public static class ElGamal
        extends JDKKeyPairGenerator
    {
        ElGamalKeyGenerationParameters  param;
        ElGamalKeyPairGenerator         engine = new ElGamalKeyPairGenerator();
        int                             strength = 1024;
        int                             certainty = 20;
        SecureRandom                    random = new SecureRandom();
        boolean                         initialised = false;

        public ElGamal()
        {
            super("ElGamal");
        }

        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            this.strength = strength;
            this.random = random;
        }

        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof ElGamalParameterSpec) && !(params instanceof DHParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a DHParameterSpec or an ElGamalParameterSpec");
            }
            
            if (params instanceof ElGamalParameterSpec)
            {
                ElGamalParameterSpec     elParams = (ElGamalParameterSpec)params;

                param = new ElGamalKeyGenerationParameters(random, new ElGamalParameters(elParams.getP(), elParams.getG()));
            }
            else
            {
                DHParameterSpec     dhParams = (DHParameterSpec)params;

                param = new ElGamalKeyGenerationParameters(random, new ElGamalParameters(dhParams.getP(), dhParams.getG(), dhParams.getL()));
            }

            engine.init(param);
            initialised = true;
        }

        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                ElGamalParametersGenerator   pGen = new ElGamalParametersGenerator();

                pGen.init(strength, certainty, random);
                param = new ElGamalKeyGenerationParameters(random, pGen.generateParameters());
                engine.init(param);
                initialised = true;
            }

            AsymmetricCipherKeyPair         pair = engine.generateKeyPair();
            ElGamalPublicKeyParameters      pub = (ElGamalPublicKeyParameters)pair.getPublic();
            ElGamalPrivateKeyParameters     priv = (ElGamalPrivateKeyParameters)pair.getPrivate();

            return new KeyPair(new JCEElGamalPublicKey(pub),
                               new JCEElGamalPrivateKey(priv));
        }
    }

    public static class GOST3410
        extends JDKKeyPairGenerator
    {
        GOST3410KeyGenerationParameters param;
        GOST3410KeyPairGenerator        engine = new GOST3410KeyPairGenerator();
        GOST3410ParameterSpec           gost3410Params;
        int                             strength = 1024;
        SecureRandom                    random = null;
        boolean                         initialised = false;

        public GOST3410()
        {
            super("GOST3410");
        }

        public void initialize(
            int             strength,
            SecureRandom    random)
        {
            this.strength = strength;
            this.random = random;
        }
    
        private void init(
            GOST3410ParameterSpec gParams,
            SecureRandom          random)
        {
            GOST3410PublicKeyParameterSetSpec spec = gParams.getPublicKeyParameters();
            
            param = new GOST3410KeyGenerationParameters(random, new GOST3410Parameters(spec.getP(), spec.getQ(), spec.getA()));
            
            engine.init(param);
            
            initialised = true;
            gost3410Params = gParams;
        }
        
        public void initialize(
            AlgorithmParameterSpec  params,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(params instanceof GOST3410ParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("parameter object not a GOST3410ParameterSpec");
            }
            
            init((GOST3410ParameterSpec)params, random);
        }

        public KeyPair generateKeyPair()
        {
            if (!initialised)
            {
                init(new GOST3410ParameterSpec(CryptoProObjectIdentifiers.gostR3410_94_CryptoPro_A.getId()), new SecureRandom());
            }
            
            AsymmetricCipherKeyPair   pair = engine.generateKeyPair();
            GOST3410PublicKeyParameters  pub = (GOST3410PublicKeyParameters)pair.getPublic();
            GOST3410PrivateKeyParameters priv = (GOST3410PrivateKeyParameters)pair.getPrivate();
            
            return new KeyPair(new JDKGOST3410PublicKey(pub, gost3410Params), new JDKGOST3410PrivateKey(priv, gost3410Params));
        }
   }
}
