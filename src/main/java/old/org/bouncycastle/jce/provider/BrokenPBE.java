package old.org.bouncycastle.jce.provider;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.PBEParameterSpec;

import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.PBEParametersGenerator;
import old.org.bouncycastle.crypto.digests.MD5Digest;
import old.org.bouncycastle.crypto.digests.RIPEMD160Digest;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import old.org.bouncycastle.crypto.generators.PKCS5S1ParametersGenerator;
import old.org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import old.org.bouncycastle.crypto.params.KeyParameter;
import old.org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Generator for PBE derived keys and ivs as defined by PKCS 12 V1.0,
 * with a bug affecting 180 bit plus keys - this class is only here to
 * allow smooth migration of the version 0 keystore to version 1. Don't
 * use it (it won't be staying around).
 * <p>
 * The document this implementation is based on can be found at
 * <a href=http://www.rsasecurity.com/rsalabs/pkcs/pkcs-12/index.html>
 * RSA's PKCS12 Page</a>
 */
class OldPKCS12ParametersGenerator
    extends PBEParametersGenerator
{
    public static final int KEY_MATERIAL = 1;
    public static final int IV_MATERIAL  = 2;
    public static final int MAC_MATERIAL = 3;

    private Digest digest;

    private int     u;
    private int     v;

    /**
     * Construct a PKCS 12 Parameters generator. This constructor will
     * accept MD5, SHA1, and RIPEMD160.
     *
     * @param digest the digest to be used as the source of derived keys.
     * @exception IllegalArgumentException if an unknown digest is passed in.
     */
    public OldPKCS12ParametersGenerator(
        Digest  digest)
    {
        this.digest = digest;
        if (digest instanceof MD5Digest)
        {
            u = 128 / 8;
            v = 512 / 8;
        }
        else if (digest instanceof SHA1Digest)
        {
            u = 160 / 8;
            v = 512 / 8;
        }
        else if (digest instanceof RIPEMD160Digest)
        {
            u = 160 / 8;
            v = 512 / 8;
        }
        else
        {
            throw new IllegalArgumentException("Digest " + digest.getAlgorithmName() + " unsupported");
        }
    }

    /**
     * add a + b + 1, returning the result in a. The a value is treated
     * as a BigInteger of length (b.length * 8) bits. The result is 
     * modulo 2^b.length in case of overflow.
     */
    private void adjust(
        byte[]  a,
        int     aOff,
        byte[]  b)
    {
        int  x = (b[b.length - 1] & 0xff) + (a[aOff + b.length - 1] & 0xff) + 1;

        a[aOff + b.length - 1] = (byte)x;
        x >>>= 8;

        for (int i = b.length - 2; i >= 0; i--)
        {
            x += (b[i] & 0xff) + (a[aOff + i] & 0xff);
            a[aOff + i] = (byte)x;
            x >>>= 8;
        }
    }

    /**
     * generation of a derived key ala PKCS12 V1.0.
     */
    private byte[] generateDerivedKey(
        int idByte,
        int n)
    {
        byte[]  D = new byte[v];
        byte[]  dKey = new byte[n];

        for (int i = 0; i != D.length; i++)
        {
            D[i] = (byte)idByte;
        }

        byte[]  S;

        if ((salt != null) && (salt.length != 0))
        {
            S = new byte[v * ((salt.length + v - 1) / v)];

            for (int i = 0; i != S.length; i++)
            {
                S[i] = salt[i % salt.length];
            }
        }
        else
        {
            S = new byte[0];
        }

        byte[]  P;

        if ((password != null) && (password.length != 0))
        {
            P = new byte[v * ((password.length + v - 1) / v)];

            for (int i = 0; i != P.length; i++)
            {
                P[i] = password[i % password.length];
            }
        }
        else
        {
            P = new byte[0];
        }

        byte[]  I = new byte[S.length + P.length];

        System.arraycopy(S, 0, I, 0, S.length);
        System.arraycopy(P, 0, I, S.length, P.length);

        byte[]  B = new byte[v];
        int     c = (n + u - 1) / u;

        for (int i = 1; i <= c; i++)
        {
            byte[]  A = new byte[u];

            digest.update(D, 0, D.length);
            digest.update(I, 0, I.length);
            digest.doFinal(A, 0);
            for (int j = 1; j != iterationCount; j++)
            {
                digest.update(A, 0, A.length);
                digest.doFinal(A, 0);
            }

            for (int j = 0; j != B.length; j++)
            {
                B[i] = A[j % A.length];
            }

            for (int j = 0; j != I.length / v; j++)
            {
                adjust(I, j * v, B);
            }

            if (i == c)
            {
                System.arraycopy(A, 0, dKey, (i - 1) * u, dKey.length - ((i - 1) * u));
            }
            else
            {
                System.arraycopy(A, 0, dKey, (i - 1) * u, A.length);
            }
        }

        return dKey;
    }

    /**
     * Generate a key parameter derived from the password, salt, and iteration
     * count we are currently initialised with.
     *
     * @param keySize the size of the key we want (in bits)
     * @return a KeyParameter object.
     */
    public CipherParameters generateDerivedParameters(
        int keySize)
    {
        keySize = keySize / 8;

        byte[]  dKey = generateDerivedKey(KEY_MATERIAL, keySize);

        return new KeyParameter(dKey, 0, keySize);
    }

    /**
     * Generate a key with initialisation vector parameter derived from
     * the password, salt, and iteration count we are currently initialised
     * with.
     *
     * @param keySize the size of the key we want (in bits)
     * @param ivSize the size of the iv we want (in bits)
     * @return a ParametersWithIV object.
     */
    public CipherParameters generateDerivedParameters(
        int     keySize,
        int     ivSize)
    {
        keySize = keySize / 8;
        ivSize = ivSize / 8;

        byte[]  dKey = generateDerivedKey(KEY_MATERIAL, keySize);

        byte[]  iv = generateDerivedKey(IV_MATERIAL, ivSize);

        return new ParametersWithIV(new KeyParameter(dKey, 0, keySize), iv, 0, ivSize);
    }

    /**
     * Generate a key parameter for use with a MAC derived from the password,
     * salt, and iteration count we are currently initialised with.
     *
     * @param keySize the size of the key we want (in bits)
     * @return a KeyParameter object.
     */
    public CipherParameters generateDerivedMacParameters(
        int keySize)
    {
        keySize = keySize / 8;

        byte[]  dKey = generateDerivedKey(MAC_MATERIAL, keySize);

        return new KeyParameter(dKey, 0, keySize);
    }
}

public interface BrokenPBE
{
    //
    // PBE Based encryption constants - by default we do PKCS12 with SHA-1
    //
    static final int        MD5         = 0;
    static final int        SHA1        = 1;
    static final int        RIPEMD160   = 2;

    static final int        PKCS5S1     = 0;
    static final int        PKCS5S2     = 1;
    static final int        PKCS12      = 2;
    static final int        OLD_PKCS12  = 3;

    /**
     * uses the appropriate mixer to generate the key and IV if neccessary.
     */
    static class Util
    {
        /**
         * a faulty parity routine...
         *
         * @param bytes the byte array to set the parity on.
         */
        static private void setOddParity(
            byte[] bytes)
        {
            for (int i = 0; i < bytes.length; i++)
            {
                int b = bytes[i];
                bytes[i] = (byte)((b & 0xfe) |
                                (((b >> 1) ^
                                (b >> 2) ^
                                (b >> 3) ^
                                (b >> 4) ^
                                (b >> 5) ^
                                (b >> 6) ^
                                (b >> 7)) ^ 0x01));
            }
        }

        static private PBEParametersGenerator makePBEGenerator(
            int                     type,
            int                     hash)
        {
            PBEParametersGenerator  generator;
    
            if (type == PKCS5S1)
            {
                switch (hash)
                {
                case MD5:
                    generator = new PKCS5S1ParametersGenerator(new MD5Digest());
                    break;
                case SHA1:
                    generator = new PKCS5S1ParametersGenerator(new SHA1Digest());
                    break;
                default:
                    throw new IllegalStateException("PKCS5 scheme 1 only supports only MD5 and SHA1.");
                }
            }
            else if (type == PKCS5S2)
            {
                generator = new PKCS5S2ParametersGenerator();
            }
            else if (type == OLD_PKCS12)
            {
                switch (hash)
                {
                case MD5:
                    generator = new OldPKCS12ParametersGenerator(new MD5Digest());
                    break;
                case SHA1:
                    generator = new OldPKCS12ParametersGenerator(new SHA1Digest());
                    break;
                case RIPEMD160:
                    generator = new OldPKCS12ParametersGenerator(new RIPEMD160Digest());
                    break;
                default:
                    throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }
            else
            {
                switch (hash)
                {
                case MD5:
                    generator = new PKCS12ParametersGenerator(new MD5Digest());
                    break;
                case SHA1:
                    generator = new PKCS12ParametersGenerator(new SHA1Digest());
                    break;
                case RIPEMD160:
                    generator = new PKCS12ParametersGenerator(new RIPEMD160Digest());
                    break;
                default:
                    throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }
    
            return generator;
        }

        /**
         * construct a key and iv (if neccessary) suitable for use with a 
         * Cipher.
         */
        static CipherParameters makePBEParameters(
            JCEPBEKey               pbeKey,
            AlgorithmParameterSpec  spec,
            int                     type,
            int                     hash,
            String                  targetAlgorithm,
            int                     keySize,
            int                     ivSize)
        {
            if ((spec == null) || !(spec instanceof PBEParameterSpec))
            {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
    
            PBEParameterSpec        pbeParam = (PBEParameterSpec)spec;
            PBEParametersGenerator  generator = makePBEGenerator(type, hash);
            byte[]                  key = pbeKey.getEncoded();
            CipherParameters        param;
    
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());

            if (ivSize != 0)
            {
                param = generator.generateDerivedParameters(keySize, ivSize);
            }
            else
            {
                param = generator.generateDerivedParameters(keySize);
            }

            if (targetAlgorithm.startsWith("DES"))
            {
                if (param instanceof ParametersWithIV)
                {
                    KeyParameter    kParam = (KeyParameter)((ParametersWithIV)param).getParameters();

                    setOddParity(kParam.getKey());
                }
                else
                {
                    KeyParameter    kParam = (KeyParameter)param;

                    setOddParity(kParam.getKey());
                }
            }

            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }

            return param;
        }

        /**
         * generate a PBE based key suitable for a MAC algorithm, the
         * key size is chosen according the MAC size, or the hashing algorithm,
         * whichever is greater.
         */
        static CipherParameters makePBEMacParameters(
            JCEPBEKey               pbeKey,
            AlgorithmParameterSpec  spec,
            int                     type,
            int                     hash,
            int                     keySize)
        {
            if ((spec == null) || !(spec instanceof PBEParameterSpec))
            {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
    
            PBEParameterSpec        pbeParam = (PBEParameterSpec)spec;
            PBEParametersGenerator  generator = makePBEGenerator(type, hash);
            byte[]                  key = pbeKey.getEncoded();
            CipherParameters        param;
    
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());

            param = generator.generateDerivedMacParameters(keySize);
    
            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }

            return param;
        }
    }
}
