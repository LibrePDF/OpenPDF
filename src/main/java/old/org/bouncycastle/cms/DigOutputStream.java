package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

class DigOutputStream extends OutputStream
{
    private final MessageDigest dig;

    DigOutputStream(MessageDigest dig)
    {
        this.dig = dig;
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        dig.update(b, off, len);
    }

    public void write(int b) throws IOException
    {
        dig.update((byte) b);
    }
}