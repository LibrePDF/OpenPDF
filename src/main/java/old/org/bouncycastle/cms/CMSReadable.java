package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;

interface CMSReadable
{
    public InputStream getInputStream()
        throws IOException, CMSException;
}
