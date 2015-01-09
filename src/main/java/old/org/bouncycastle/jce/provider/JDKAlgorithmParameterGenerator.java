package old.org.bouncycastle.jce.provider;

import java.security.AlgorithmParameterGeneratorSpi;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

import javax.crypto.spec.DHGenParameterSpec;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;

import old.org.bouncycastle.crypto.generators.DHParametersGenerator;
import old.org.bouncycastle.crypto.generators.DSAParametersGenerator;
import old.org.bouncycastle.crypto.generators.ElGamalParametersGenerator;
import old.org.bouncycastle.crypto.generators.GOST3410ParametersGenerator;
import old.org.bouncycastle.crypto.params.DHParameters;
import old.org.bouncycastle.crypto.params.DSAParameters;
import old.org.bouncycastle.crypto.params.ElGamalParameters;
import old.org.bouncycastle.crypto.params.GOST3410Parameters;
import old.org.bouncycastle.jce.spec.GOST3410ParameterSpec;
import old.org.bouncycastle.jce.spec.GOST3410PublicKeyParameterSetSpec;

public abstract class JDKAlgorithmParameterGenerator
    extends AlgorithmParameterGeneratorSpi
{
    protected SecureRandom  random;
    protected int           strength = 1024;

    protected void engineInit(
        int             strength,
        SecureRandom    random)
    {
        this.strength = strength;
        this.random = random;
    }

    public static class DH
        extends JDKAlgorithmParameterGenerator
    {
        private int l = 0;

        protected void engineInit(
            AlgorithmParameterSpec  genParamSpec,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(genParamSpec instanceof DHGenParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("DH parameter generator requires a DHGenParameterSpec for initialisation");
            }
            DHGenParameterSpec  spec = (DHGenParameterSpec)genParamSpec;

            this.strength = spec.getPrimeSize();
            this.l = spec.getExponentSize();
            this.random = random;
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            DHParametersGenerator        pGen = new DHParametersGenerator();

            if (random != null)
            {
                pGen.init(strength, 20, random);
            }
            else
            {
                pGen.init(strength, 20, new SecureRandom());
            }

            DHParameters                p = pGen.generateParameters();

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("DH", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new DHParameterSpec(p.getP(), p.getG(), l));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class DSA
        extends JDKAlgorithmParameterGenerator
    {
        protected void engineInit(
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

        protected void engineInit(
            AlgorithmParameterSpec  genParamSpec,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for DSA parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            DSAParametersGenerator pGen = new DSAParametersGenerator();

            if (random != null)
            {
                pGen.init(strength, 20, random);
            }
            else
            {
                pGen.init(strength, 20, new SecureRandom());
            }

            DSAParameters p = pGen.generateParameters();

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("DSA", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new DSAParameterSpec(p.getP(), p.getQ(), p.getG()));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class GOST3410
        extends JDKAlgorithmParameterGenerator
    {
        protected void engineInit(
                AlgorithmParameterSpec  genParamSpec,
                SecureRandom            random)
        throws InvalidAlgorithmParameterException
        {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for GOST3410 parameter generation.");
        }
        
        protected AlgorithmParameters engineGenerateParameters()
        {
            GOST3410ParametersGenerator pGen = new GOST3410ParametersGenerator();
            
            if (random != null)
            {
                pGen.init(strength, 2, random);
            }
            else
            {
                pGen.init(strength, 2, new SecureRandom());
            }
            
            GOST3410Parameters p = pGen.generateParameters();
            
            AlgorithmParameters params;
            
            try
            {
                params = AlgorithmParameters.getInstance("GOST3410", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new GOST3410ParameterSpec(new GOST3410PublicKeyParameterSetSpec(p.getP(), p.getQ(), p.getA())));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }
            
            return params;
        }
    }
    
    public static class ElGamal
        extends JDKAlgorithmParameterGenerator
    {
        private int l = 0;
        
        protected void engineInit(
            AlgorithmParameterSpec  genParamSpec,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (!(genParamSpec instanceof DHGenParameterSpec))
            {
                throw new InvalidAlgorithmParameterException("DH parameter generator requires a DHGenParameterSpec for initialisation");
            }
            DHGenParameterSpec  spec = (DHGenParameterSpec)genParamSpec;

            this.strength = spec.getPrimeSize();
            this.l = spec.getExponentSize();
            this.random = random;
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            ElGamalParametersGenerator pGen = new ElGamalParametersGenerator();

            if (random != null)
            {
                pGen.init(strength, 20, random);
            }
            else
            {
                pGen.init(strength, 20, new SecureRandom());
            }

            ElGamalParameters p = pGen.generateParameters();

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("ElGamal", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new DHParameterSpec(p.getP(), p.getG(), l));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class DES
        extends JDKAlgorithmParameterGenerator
    {
        protected void engineInit(
            AlgorithmParameterSpec  genParamSpec,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for DES parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            byte[]  iv = new byte[8];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            AlgorithmParameters params;

            try
            {
                params = AlgorithmParameters.getInstance("DES", BouncyCastleProvider.PROVIDER_NAME);
                params.init(new IvParameterSpec(iv));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }

            return params;
        }
    }

    public static class RC2
        extends JDKAlgorithmParameterGenerator
    {
        RC2ParameterSpec    spec = null;

        protected void engineInit(
            AlgorithmParameterSpec  genParamSpec,
            SecureRandom            random)
            throws InvalidAlgorithmParameterException
        {
            if (genParamSpec instanceof RC2ParameterSpec)
            {
                spec = (RC2ParameterSpec)genParamSpec;
                return;
            }

            throw new InvalidAlgorithmParameterException("No supported AlgorithmParameterSpec for RC2 parameter generation.");
        }

        protected AlgorithmParameters engineGenerateParameters()
        {
            AlgorithmParameters params;

            if (spec == null)
            {
                byte[]  iv = new byte[8];

                if (random == null)
                {
                    random = new SecureRandom();
                }

                random.nextBytes(iv);

                try
                {
                    params = AlgorithmParameters.getInstance("RC2", BouncyCastleProvider.PROVIDER_NAME);
                    params.init(new IvParameterSpec(iv));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.getMessage());
                }
            }
            else
            {
                try
                {
                    params = AlgorithmParameters.getInstance("RC2", BouncyCastleProvider.PROVIDER_NAME);
                    params.init(spec);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e.getMessage());
                }
            }

            return params;
        }
    }
}
