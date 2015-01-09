package old.org.bouncycastle.x509;

import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.util.Arrays;
import old.org.bouncycastle.util.Selector;
import old.org.bouncycastle.x509.extension.X509ExtensionUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRL;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLSelector;

/**
 * This class is a Selector implementation for X.509 certificate revocation
 * lists.
 * 
 * @see old.org.bouncycastle.util.Selector
 * @see old.org.bouncycastle.x509.X509Store
 * @see old.org.bouncycastle.jce.provider.X509StoreCRLCollection
 */
public class X509CRLStoreSelector
    extends X509CRLSelector
    implements Selector
{
    private boolean deltaCRLIndicator = false;

    private boolean completeCRLEnabled = false;

    private BigInteger maxBaseCRLNumber = null;

    private byte[] issuingDistributionPoint = null;

    private boolean issuingDistributionPointEnabled = false;

    private X509AttributeCertificate attrCertChecking;

    /**
     * Returns if the issuing distribution point criteria should be applied.
     * Defaults to <code>false</code>.
     * <p>
     * You may also set the issuing distribution point criteria if not a missing
     * issuing distribution point should be assumed.
     * 
     * @return Returns if the issuing distribution point check is enabled.
     */
    public boolean isIssuingDistributionPointEnabled()
    {
        return issuingDistributionPointEnabled;
    }

    /**
     * Enables or disables the issuing distribution point check.
     * 
     * @param issuingDistributionPointEnabled <code>true</code> to enable the
     *            issuing distribution point check.
     */
    public void setIssuingDistributionPointEnabled(
        boolean issuingDistributionPointEnabled)
    {
        this.issuingDistributionPointEnabled = issuingDistributionPointEnabled;
    }

    /**
     * Sets the attribute certificate being checked. This is not a criterion.
     * Rather, it is optional information that may help a {@link X509Store} find
     * CRLs that would be relevant when checking revocation for the specified
     * attribute certificate. If <code>null</code> is specified, then no such
     * optional information is provided.
     * 
     * @param attrCert the <code>X509AttributeCertificate</code> being checked (or
     *            <code>null</code>)
     * @see #getAttrCertificateChecking()
     */
    public void setAttrCertificateChecking(X509AttributeCertificate attrCert)
    {
        attrCertChecking = attrCert;
    }

    /**
     * Returns the attribute certificate being checked.
     * 
     * @return Returns the attribute certificate being checked.
     * @see #setAttrCertificateChecking(X509AttributeCertificate)
     */
    public X509AttributeCertificate getAttrCertificateChecking()
    {
        return attrCertChecking;
    }

    public boolean match(Object obj)
    {
        if (!(obj instanceof X509CRL))
        {
            return false;
        }
        X509CRL crl = (X509CRL)obj;
        DERInteger dci = null;
        try
        {
            byte[] bytes = crl
                .getExtensionValue(X509Extensions.DeltaCRLIndicator.getId());
            if (bytes != null)
            {
                dci = DERInteger.getInstance(X509ExtensionUtil
                    .fromExtensionValue(bytes));
            }
        }
        catch (Exception e)
        {
            return false;
        }
        if (isDeltaCRLIndicatorEnabled())
        {
            if (dci == null)
            {
                return false;
            }
        }
        if (isCompleteCRLEnabled())
        {
            if (dci != null)
            {
                return false;
            }
        }
        if (dci != null)
        {

            if (maxBaseCRLNumber != null)
            {
                if (dci.getPositiveValue().compareTo(maxBaseCRLNumber) == 1)
                {
                    return false;
                }
            }
        }
        if (issuingDistributionPointEnabled)
        {
            byte[] idp = crl
                .getExtensionValue(X509Extensions.IssuingDistributionPoint
                    .getId());
            if (issuingDistributionPoint == null)
            {
                if (idp != null)
                {
                    return false;
                }
            }
            else
            {
                if (!Arrays.areEqual(idp, issuingDistributionPoint))
                {
                    return false;
                }
            }

        }
        return super.match((X509CRL)obj);
    }

    public boolean match(CRL crl)
    {
        return match((Object)crl);
    }

    /**
     * Returns if this selector must match CRLs with the delta CRL indicator
     * extension set. Defaults to <code>false</code>.
     * 
     * @return Returns <code>true</code> if only CRLs with the delta CRL
     *         indicator extension are selected.
     */
    public boolean isDeltaCRLIndicatorEnabled()
    {
        return deltaCRLIndicator;
    }

    /**
     * If this is set to <code>true</code> the CRL reported contains the delta
     * CRL indicator CRL extension.
     * <p>
     * {@link #setCompleteCRLEnabled(boolean)} and
     * {@link #setDeltaCRLIndicatorEnabled(boolean)} excluded each other.
     * 
     * @param deltaCRLIndicator <code>true</code> if the delta CRL indicator
     *            extension must be in the CRL.
     */
    public void setDeltaCRLIndicatorEnabled(boolean deltaCRLIndicator)
    {
        this.deltaCRLIndicator = deltaCRLIndicator;
    }

    /**
     * Returns an instance of this from a <code>X509CRLSelector</code>.
     * 
     * @param selector A <code>X509CRLSelector</code> instance.
     * @return An instance of an <code>X509CRLStoreSelector</code>.
     * @exception IllegalArgumentException if selector is null or creation
     *                fails.
     */
    public static X509CRLStoreSelector getInstance(X509CRLSelector selector)
    {
        if (selector == null)
        {
            throw new IllegalArgumentException(
                "cannot create from null selector");
        }
        X509CRLStoreSelector cs = new X509CRLStoreSelector();
        cs.setCertificateChecking(selector.getCertificateChecking());
        cs.setDateAndTime(selector.getDateAndTime());
        try
        {
            cs.setIssuerNames(selector.getIssuerNames());
        }
        catch (IOException e)
        {
            // cannot happen
            throw new IllegalArgumentException(e.getMessage());
        }
        cs.setIssuers(selector.getIssuers());
        cs.setMaxCRLNumber(selector.getMaxCRL());
        cs.setMinCRLNumber(selector.getMinCRL());
        return cs;
    }
    
    public Object clone()
    {
        X509CRLStoreSelector sel = X509CRLStoreSelector.getInstance(this);
        sel.deltaCRLIndicator = deltaCRLIndicator;
        sel.completeCRLEnabled = completeCRLEnabled;
        sel.maxBaseCRLNumber = maxBaseCRLNumber;
        sel.attrCertChecking = attrCertChecking;
        sel.issuingDistributionPointEnabled = issuingDistributionPointEnabled;
        sel.issuingDistributionPoint = Arrays.clone(issuingDistributionPoint);
        return sel;
    }

    /**
     * If <code>true</code> only complete CRLs are returned. Defaults to
     * <code>false</code>.
     * 
     * @return <code>true</code> if only complete CRLs are returned.
     */
    public boolean isCompleteCRLEnabled()
    {
        return completeCRLEnabled;
    }

    /**
     * If set to <code>true</code> only complete CRLs are returned.
     * <p>
     * {@link #setCompleteCRLEnabled(boolean)} and
     * {@link #setDeltaCRLIndicatorEnabled(boolean)} excluded each other.
     * 
     * @param completeCRLEnabled <code>true</code> if only complete CRLs
     *            should be returned.
     */
    public void setCompleteCRLEnabled(boolean completeCRLEnabled)
    {
        this.completeCRLEnabled = completeCRLEnabled;
    }

    /**
     * Get the maximum base CRL number. Defaults to <code>null</code>.
     * 
     * @return Returns the maximum base CRL number.
     * @see #setMaxBaseCRLNumber(BigInteger)
     */
    public BigInteger getMaxBaseCRLNumber()
    {
        return maxBaseCRLNumber;
    }

    /**
     * Sets the maximum base CRL number. Setting to <code>null</code> disables
     * this cheack.
     * <p>
     * This is only meaningful for delta CRLs. Complete CRLs must have a CRL
     * number which is greater or equal than the base number of the
     * corresponding CRL.
     * 
     * @param maxBaseCRLNumber The maximum base CRL number to set.
     */
    public void setMaxBaseCRLNumber(BigInteger maxBaseCRLNumber)
    {
        this.maxBaseCRLNumber = maxBaseCRLNumber;
    }

    /**
     * Returns the issuing distribution point. Defaults to <code>null</code>,
     * which is a missing issuing distribution point extension.
     * <p>
     * The internal byte array is cloned before it is returned.
     * <p>
     * The criteria must be enable with
     * {@link #setIssuingDistributionPointEnabled(boolean)}.
     * 
     * @return Returns the issuing distribution point.
     * @see #setIssuingDistributionPoint(byte[])
     */
    public byte[] getIssuingDistributionPoint()
    {
        return Arrays.clone(issuingDistributionPoint);
    }

    /**
     * Sets the issuing distribution point.
     * <p>
     * The issuing distribution point extension is a CRL extension which
     * identifies the scope and the distribution point of a CRL. The scope
     * contains among others information about revocation reasons contained in
     * the CRL. Delta CRLs and complete CRLs must have matching issuing
     * distribution points.
     * <p>
     * The byte array is cloned to protect against subsequent modifications.
     * <p>
     * You must also enable or disable this criteria with
     * {@link #setIssuingDistributionPointEnabled(boolean)}.
     * 
     * @param issuingDistributionPoint The issuing distribution point to set.
     *            This is the DER encoded OCTET STRING extension value.
     * @see #getIssuingDistributionPoint()
     */
    public void setIssuingDistributionPoint(byte[] issuingDistributionPoint)
    {
        this.issuingDistributionPoint = Arrays.clone(issuingDistributionPoint);
    }
}
