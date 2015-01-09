package old.org.bouncycastle.cert.jcajce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import old.org.bouncycastle.util.CollectionStore;
import old.org.bouncycastle.x509.X509AttributeCertificate;

/**
 * Class for storing Attribute Certificates for later lookup.
 * <p>
 * The class will convert X509AttributeCertificate objects into X509AttributeCertificateHolder objects.
 * </p>
 */
public class JcaAttrCertStore
    extends CollectionStore
{
    /**
     * Basic constructor.
     *
     * @param collection - initial contents for the store, this is copied.
     */
    public JcaAttrCertStore(Collection collection)
        throws IOException
    {
        super(convertCerts(collection));
    }

    public JcaAttrCertStore(X509AttributeCertificate attrCert)
        throws IOException
    {
        this(Collections.singletonList(attrCert));
    }

    private static Collection convertCerts(Collection collection)
        throws IOException
    {
        List list = new ArrayList(collection.size());

        for (Iterator it = collection.iterator(); it.hasNext();)
        {
            Object o = it.next();

            if (o instanceof X509AttributeCertificate)
            {
                X509AttributeCertificate cert = (X509AttributeCertificate)o;

                list.add(new JcaX509AttributeCertificateHolder(cert));
            }
            else
            {
                list.add(o);
            }
        }

        return list;
    }
}
