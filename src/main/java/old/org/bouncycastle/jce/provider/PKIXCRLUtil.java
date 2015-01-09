package old.org.bouncycastle.jce.provider;

import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.PKIXParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.util.StoreException;
import old.org.bouncycastle.x509.ExtendedPKIXParameters;
import old.org.bouncycastle.x509.X509CRLStoreSelector;
import old.org.bouncycastle.x509.X509Store;

public class PKIXCRLUtil
{
    public Set findCRLs(X509CRLStoreSelector crlselect, ExtendedPKIXParameters paramsPKIX, Date currentDate)
        throws AnnotatedException
    {
        Set initialSet = new HashSet();

        // get complete CRL(s)
        try
        {
            initialSet.addAll(findCRLs(crlselect, paramsPKIX.getAdditionalStores()));
            initialSet.addAll(findCRLs(crlselect, paramsPKIX.getStores()));
            initialSet.addAll(findCRLs(crlselect, paramsPKIX.getCertStores()));
        }
        catch (AnnotatedException e)
        {
            throw new AnnotatedException("Exception obtaining complete CRLs.", e);
        }

        Set finalSet = new HashSet();
        Date validityDate = currentDate;

        if (paramsPKIX.getDate() != null)
        {
            validityDate = paramsPKIX.getDate();
        }

        // based on RFC 5280 6.3.3
        for (Iterator it = initialSet.iterator(); it.hasNext();)
        {
            X509CRL crl = (X509CRL)it.next();

            if (crl.getNextUpdate().after(validityDate))
            {
                X509Certificate cert = crlselect.getCertificateChecking();

                if (cert != null)
                {
                    if (crl.getThisUpdate().before(cert.getNotAfter()))
                    {
                        finalSet.add(crl);
                    }
                }
                else
                {
                    finalSet.add(crl);
                }
            }
        }

        return finalSet;
    }

    public Set findCRLs(X509CRLStoreSelector crlselect, PKIXParameters paramsPKIX)
        throws AnnotatedException
    {
        Set completeSet = new HashSet();

        // get complete CRL(s)
        try
        {
            completeSet.addAll(findCRLs(crlselect, paramsPKIX.getCertStores()));
        }
        catch (AnnotatedException e)
        {
            throw new AnnotatedException("Exception obtaining complete CRLs.", e);
        }

        return completeSet;
    }

/**
     * Return a Collection of all CRLs found in the X509Store's that are
     * matching the crlSelect criteriums.
     *
     * @param crlSelect a {@link X509CRLStoreSelector} object that will be used
     *            to select the CRLs
     * @param crlStores a List containing only
     *            {@link old.org.bouncycastle.x509.X509Store  X509Store} objects.
     *            These are used to search for CRLs
     *
     * @return a Collection of all found {@link java.security.cert.X509CRL X509CRL} objects. May be
     *         empty but never <code>null</code>.
     */
    private final Collection findCRLs(X509CRLStoreSelector crlSelect,
        List crlStores) throws AnnotatedException
    {
        Set crls = new HashSet();
        Iterator iter = crlStores.iterator();

        AnnotatedException lastException = null;
        boolean foundValidStore = false;

        while (iter.hasNext())
        {
            Object obj = iter.next();

            if (obj instanceof X509Store)
            {
                X509Store store = (X509Store)obj;

                try
                {
                    crls.addAll(store.getMatches(crlSelect));
                    foundValidStore = true;
                }
                catch (StoreException e)
                {
                    lastException = new AnnotatedException(
                        "Exception searching in X.509 CRL store.", e);
                }
            }
            else
            {
                CertStore store = (CertStore)obj;

                try
                {
                    crls.addAll(store.getCRLs(crlSelect));
                    foundValidStore = true;
                }
                catch (CertStoreException e)
                {
                    lastException = new AnnotatedException(
                        "Exception searching in X.509 CRL store.", e);
                }
            }
        }
        if (!foundValidStore && lastException != null)
        {
            throw lastException;
        }
        return crls;
    }

}
