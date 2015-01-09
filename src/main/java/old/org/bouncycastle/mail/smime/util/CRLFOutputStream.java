package old.org.bouncycastle.mail.smime.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CRLFOutputStream extends FilterOutputStream
{
    protected int lastb;
    protected static byte newline[];

    public CRLFOutputStream(OutputStream outputstream)
    {
        super(outputstream);
        lastb = -1;
    }

    public void write(int i)
        throws IOException
    {
        if (i == '\r')
        {
            out.write(newline);
        }
        else if (i == '\n')
        {
            if (lastb != '\r')
            {
                out.write(newline);
            }
        }
        else
        {
           out.write(i);
        }
        
        lastb = i;
    }

    public void write(byte[] buf)
        throws IOException
    {
        this.write(buf, 0, buf.length);
    }

    public void write(byte buf[], int off, int len)
        throws IOException
    {
        for (int i = off; i != off + len; i++)
        {
            this.write(buf[i]);
        }
    }

    public void writeln()
        throws IOException
    {
        super.out.write(newline);
    }

    static 
    {
        newline = new byte[2];
        newline[0] = '\r';
        newline[1] = '\n';
    }
}
