/**
 * 
 */
package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;

class NullOutputStream
    extends OutputStream
{
    public void write(byte[] buf)
        throws IOException
    {
        // do nothing
    }

    public void write(byte[] buf, int off, int len)
        throws IOException
    {
        // do nothing
    }
    
    public void write(int b) throws IOException
    {
        // do nothing
    }
}