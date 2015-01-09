package old.org.bouncycastle.crypto.modes.gcm;

import old.org.bouncycastle.util.Arrays;

public class BasicGCMExponentiator implements GCMExponentiator
{
    private byte[] x;

    public void init(byte[] x)
    {
        this.x = Arrays.clone(x);
    }

    public void exponentiateX(long pow, byte[] output)
    {
        // Initial value is little-endian 1
        byte[] y = GCMUtil.oneAsBytes();

        if (pow > 0)
        {
            byte[] powX = Arrays.clone(x);
            do
            {
                if ((pow & 1L) != 0)
                {
                    GCMUtil.multiply(y, powX);
                }
                GCMUtil.multiply(powX, powX);
                pow >>>= 1;
            }
            while (pow > 0);
        }

        System.arraycopy(y, 0, output, 0, 16);
    }
}
