package old.org.bouncycastle.cms;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEParameterSpec;

public abstract class CMSPBEKey
    implements PBEKey
{
    private char[] password;
    private byte[] salt;
    private int    iterationCount;

    protected static PBEParameterSpec getParamSpec(AlgorithmParameters algParams)
        throws InvalidAlgorithmParameterException
    {
        try
        {
            return (PBEParameterSpec)algParams.getParameterSpec(PBEParameterSpec.class);
        }
        catch (InvalidParameterSpecException e)
        {
            throw new InvalidAlgorithmParameterException("cannot process PBE spec: " + e.getMessage());
        }
    }

    public CMSPBEKey(char[] password, byte[] salt, int iterationCount)
    {
        this.password = password;
        this.salt = salt;
        this.iterationCount = iterationCount;
    }

    public CMSPBEKey(char[] password, PBEParameterSpec pbeSpec)
    {
        this(password, pbeSpec.getSalt(), pbeSpec.getIterationCount());
    }
    
    public char[] getPassword()
    {
        return password;
    }

    public byte[] getSalt()
    {
        return salt;
    }

    public int getIterationCount()
    {
        return iterationCount;
    }

    public String getAlgorithm()
    {
        return "PKCS5S2";
    }

    public String getFormat()
    {
        return "RAW";
    }

    public byte[] getEncoded()
    {
        return null;
    }

    abstract byte[] getEncoded(String algorithmOid);
}
