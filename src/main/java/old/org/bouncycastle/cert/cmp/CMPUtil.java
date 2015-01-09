package old.org.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.DEROutputStream;

class CMPUtil
{
    static void derEncodeToStream(ASN1Encodable obj, OutputStream stream)
    {
        DEROutputStream dOut = new DEROutputStream(stream);

        try
        {
            dOut.writeObject(obj);

            dOut.close();
        }
        catch (IOException e)
        {
            throw new CMPRuntimeException("unable to DER encode object: " + e.getMessage(), e);
        }
    }
}
