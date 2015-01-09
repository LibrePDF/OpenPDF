package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;

public interface SMIMEStreamingProcessor
{
    public void write(OutputStream out) 
        throws IOException;
}
