package old.org.bouncycastle.jce.spec; 

import java.security.spec.AlgorithmParameterSpec;

import old.org.bouncycastle.crypto.engines.GOST28147Engine;

/**
 * A parameter spec for the GOST-28147 cipher.
 */
public class GOST28147ParameterSpec
    implements AlgorithmParameterSpec
{
    private byte[] iv = null;
    private byte[] sBox = null;

    public GOST28147ParameterSpec(
        byte[] sBox)
    {
        this.sBox = new byte[sBox.length];
        
        System.arraycopy(sBox, 0, this.sBox, 0, sBox.length);
    }

    public GOST28147ParameterSpec(
        byte[] sBox,
        byte[] iv)
    {
        this(sBox);
        this.iv = new byte[iv.length];
        
        System.arraycopy(iv, 0, this.iv, 0, iv.length);
    }
    
    public GOST28147ParameterSpec(
        String  sBoxName)
    {
        this.sBox = GOST28147Engine.getSBox(sBoxName);
    }

    public GOST28147ParameterSpec(
        String  sBoxName,
        byte[]  iv)
    {
        this(sBoxName);
        this.iv = new byte[iv.length];
        
        System.arraycopy(iv, 0, this.iv, 0, iv.length);
    }

    public byte[] getSbox()
    {
        return sBox;
    }

    /**
     * Returns the IV or null if this parameter set does not contain an IV.
     *
     * @return the IV or null if this parameter set does not contain an IV.
     */
    public byte[] getIV()
    {
        if (iv == null)
        {
            return null;
        }

        byte[]  tmp = new byte[iv.length];

        System.arraycopy(iv, 0, tmp, 0, tmp.length);

        return tmp;
    }
}