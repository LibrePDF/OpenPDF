package old.org.bouncycastle.crypto.modes.gcm;

import old.org.bouncycastle.util.Arrays;

public class Tables1kGCMExponentiator implements GCMExponentiator
{
    // A lookup table of the power-of-two powers of 'x'
    byte[][] lookupPowX2 = new byte[64][];

    public void init(byte[] x)
    {
        // Initial value is little-endian 1
        lookupPowX2[0] = new byte[16];
        lookupPowX2[0][0] = (byte)0x80;

        lookupPowX2[1] = Arrays.clone(x); 

        for (int i = 2; i != 64; ++i)
        {
            byte[] tmp = Arrays.clone(lookupPowX2[i - 1]);
            GCMUtil.multiply(tmp, tmp);
            lookupPowX2[i] = tmp;
        }
    }

    public void exponentiateX(long pow, byte[] output)
    {
        // Initial value is little-endian 1
        byte[] y = GCMUtil.oneAsBytes();
        int powX2 = 1;

        while (pow > 0)
        {
            if ((pow & 1L) != 0)
            {
                GCMUtil.multiply(y, lookupPowX2[powX2]);
            }
            ++powX2;
            pow >>>= 1;
        }

        System.arraycopy(y, 0, output, 0, 16);
    }
}
