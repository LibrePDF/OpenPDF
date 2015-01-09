package old.org.bouncycastle.cert.jcajce;

import java.io.IOException;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import old.org.bouncycastle.cert.X509CRLHolder;
import old.org.bouncycastle.util.CollectionStore;

/**
 * Class for storing CRLs for later lookup.
 * <p>
 * The class will convert X509CRL objects into X509CRLHolder objects.
 * </p>
 */
public class JcaCRLStore
    extends CollectionStore
{
    /**
     * Basic constructor.
     *
     * @param collection - initial contents for the store, this is copied.
     */
    public JcaCRLStore(Collection collection)
        throws CRLException
    {
        super(convertCRLs(collection));
    }

    private static Collection convertCRLs(Collection collection)
        throws CRLException
    {
        List list = new ArrayList(collection.size());

        for (Iterator it = collection.iterator(); it.hasNext();)
        {
            Object crl = it.next();

            if (crl instanceof X509CRL)
            {
                try
                {
                    list.add(new X509CRLHolder(((X509CRL)crl).getEncoded()));
                }
                catch (IOException e)
                {
                    throw new CRLException("cannot read encoding: " + e.getMessage());
                    
                }
            }
            else
            {
                list.add((X509CRLHolder)crl);
            }
        }

        return list;
    }
}
