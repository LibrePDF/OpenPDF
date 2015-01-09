package old.org.bouncycastle.jcajce.io;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Mac;

public class MacOutputStream
    extends OutputStream
{
    protected Mac mac;

    public MacOutputStream(
        Mac          mac)
    {
        this.mac = mac;
    }

    public void write(int b)
        throws IOException
    {
        mac.update((byte)b);
    }

    public void write(
        byte[] b,
        int off,
        int len)
        throws IOException
    {
        mac.update(b, off, len);
    }

    public byte[] getMac()
    {
        return mac.doFinal();
    }
}
