package old.org.bouncycastle.x509;

import old.org.bouncycastle.util.Selector;

/**
 * This class is an <code>Selector</code> like implementation to select
 * certificates pairs, which are e.g. used for cross certificates. The set of
 * criteria is given from two
 * {@link old.org.bouncycastle.x509.X509CertStoreSelector}s which must be both
 * matched.
 * 
 * @see old.org.bouncycastle.x509.X509AttributeCertificate
 * @see old.org.bouncycastle.x509.X509Store
 */
public class X509CertPairStoreSelector implements Selector
{

    private X509CertStoreSelector forwardSelector;

    private X509CertStoreSelector reverseSelector;

    private X509CertificatePair certPair;

    public X509CertPairStoreSelector()
    {
    }

    /**
     * Returns the certificate pair which is used for testing on equality.
     * 
     * @return Returns the certificate pair which is checked.
     */
    public X509CertificatePair getCertPair()
    {
        return certPair;
    }

    /**
     * Set the certificate pair which is used for testing on equality.
     * 
     * @param certPair The certPairChecking to set.
     */
    public void setCertPair(X509CertificatePair certPair)
    {
        this.certPair = certPair;
    }

    /**
     * @param forwardSelector The certificate selector for the forward part in
     *            the pair.
     */
    public void setForwardSelector(X509CertStoreSelector forwardSelector)
    {
        this.forwardSelector = forwardSelector;
    }

    /**
     * @param reverseSelector The certificate selector for the reverse part in
     *            the pair.
     */
    public void setReverseSelector(X509CertStoreSelector reverseSelector)
    {
        this.reverseSelector = reverseSelector;
    }

    /**
     * Returns a clone of this selector.
     * 
     * @return A clone of this selector.
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        X509CertPairStoreSelector cln = new X509CertPairStoreSelector();

        cln.certPair = certPair;
        
        if (forwardSelector != null)
        {
            cln.setForwardSelector((X509CertStoreSelector) forwardSelector
                    .clone());
        }

        if (reverseSelector != null)
        {
            cln.setReverseSelector((X509CertStoreSelector) reverseSelector
                    .clone());
        }

        return cln;
    }

    /**
     * Decides if the given certificate pair should be selected. If
     * <code>obj</code> is not a {@link X509CertificatePair} this method
     * returns <code>false</code>.
     * 
     * @param obj The {@link X509CertificatePair} which should be tested.
     * @return <code>true</code> if the object matches this selector.
     */
    public boolean match(Object obj)
    {
        try
        {
            if (!(obj instanceof X509CertificatePair))
            {
                return false;
            }
            X509CertificatePair pair = (X509CertificatePair)obj;

            if (forwardSelector != null
                    && !forwardSelector.match((Object)pair.getForward()))
            {
                return false;
            }

            if (reverseSelector != null
                    && !reverseSelector.match((Object)pair.getReverse()))
            {
                return false;
            }

            if (certPair != null)
            {
                return certPair.equals(obj);
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Returns the certicate selector for the forward part.
     * 
     * @return Returns the certicate selector for the forward part.
     */
    public X509CertStoreSelector getForwardSelector()
    {
        return forwardSelector;
    }

    /**
     * Returns the certicate selector for the reverse part.
     * 
     * @return Returns the reverse selector for teh reverse part.
     */
    public X509CertStoreSelector getReverseSelector()
    {
        return reverseSelector;
    }
}
