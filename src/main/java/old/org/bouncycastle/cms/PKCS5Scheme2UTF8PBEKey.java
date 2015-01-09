package old.org.bouncycastle.cms;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;

import old.org.bouncycastle.crypto.PBEParametersGenerator;
import old.org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import old.org.bouncycastle.crypto.params.KeyParameter;

/**
 * PKCS5 scheme-2 - password converted to bytes using UTF-8.
 */
public class PKCS5Scheme2UTF8PBEKey
    extends CMSPBEKey
{
    public PKCS5Scheme2UTF8PBEKey(char[] password, byte[] salt, int iterationCount)
    {
        super(password, salt, iterationCount);
    }

    public PKCS5Scheme2UTF8PBEKey(char[] password, AlgorithmParameters pbeParams)
        throws InvalidAlgorithmParameterException
    {
        super(password, getParamSpec(pbeParams));
    }

    byte[] getEncoded(String algorithmOid)
    {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator();

        gen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(this.getPassword()), this.getSalt(), this.getIterationCount());

        return ((KeyParameter)gen.generateDerivedParameters(CMSEnvelopedHelper.INSTANCE.getKeySize(algorithmOid))).getKey();
    }
}