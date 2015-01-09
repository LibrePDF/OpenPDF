package old.org.bouncycastle.asn1;

import java.io.IOException;

public interface InMemoryRepresentable
{
    DERObject getLoadedObject()
        throws IOException;
}
