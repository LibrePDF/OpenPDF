package old.org.bouncycastle.cert.selector;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;

import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.Target;
import old.org.bouncycastle.asn1.x509.TargetInformation;
import old.org.bouncycastle.asn1.x509.Targets;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.cert.AttributeCertificateHolder;
import old.org.bouncycastle.cert.AttributeCertificateIssuer;
import old.org.bouncycastle.cert.X509AttributeCertificateHolder;
import old.org.bouncycastle.util.Selector;

/**
 * This class is an <code>Selector</code> like implementation to select
 * attribute certificates from a given set of criteria.
 */
public class X509AttributeCertificateSelector
    implements Selector
{

    // TODO: name constraints???

    private final AttributeCertificateHolder holder;

    private final AttributeCertificateIssuer issuer;

    private final BigInteger serialNumber;

    private final Date attributeCertificateValid;

    private final X509AttributeCertificateHolder attributeCert;

    private final Collection targetNames;

    private final Collection targetGroups;

    X509AttributeCertificateSelector(
        AttributeCertificateHolder holder,
        AttributeCertificateIssuer issuer,
        BigInteger                 serialNumber,
        Date                       attributeCertificateValid,
        X509AttributeCertificateHolder attributeCert,
        Collection                 targetNames,
        Collection                 targetGroups)
    {
        this.holder = holder;
        this.issuer = issuer;
        this.serialNumber = serialNumber;
        this.attributeCertificateValid = attributeCertificateValid;
        this.attributeCert = attributeCert;
        this.targetNames = targetNames;
        this.targetGroups = targetGroups;
    }

    /**
     * Decides if the given attribute certificate should be selected.
     *
     * @param obj The X509AttributeCertificateHolder which should be checked.
     * @return <code>true</code> if the attribute certificate is a match
     *         <code>false</code> otherwise.
     */
    public boolean match(Object obj)
    {
        if (!(obj instanceof X509AttributeCertificateHolder))
        {
            return false;
        }

        X509AttributeCertificateHolder attrCert = (X509AttributeCertificateHolder)obj;

        if (this.attributeCert != null)
        {
            if (!this.attributeCert.equals(attrCert))
            {
                return false;
            }
        }
        if (serialNumber != null)
        {
            if (!attrCert.getSerialNumber().equals(serialNumber))
            {
                return false;
            }
        }
        if (holder != null)
        {
            if (!attrCert.getHolder().equals(holder))
            {
                return false;
            }
        }
        if (issuer != null)
        {
            if (!attrCert.getIssuer().equals(issuer))
            {
                return false;
            }
        }

        if (attributeCertificateValid != null)
        {
            if (!attrCert.isValidOn(attributeCertificateValid))
            {
                return false;
            }
        }
        if (!targetNames.isEmpty() || !targetGroups.isEmpty())
        {

            X509Extension targetInfoExt = attrCert.getExtension(X509Extension.targetInformation);
            if (targetInfoExt != null)
            {
                TargetInformation targetinfo;
                try
                {
                    targetinfo = TargetInformation.getInstance(targetInfoExt.getParsedValue());
                }
                catch (IllegalArgumentException e)
                {
                    return false;
                }
                Targets[] targetss = targetinfo.getTargetsObjects();
                if (!targetNames.isEmpty())
                {
                    boolean found = false;

                    for (int i=0; i<targetss.length; i++)
                    {
                        Targets t = targetss[i];
                        Target[] targets = t.getTargets();
                        for (int j=0; j<targets.length; j++)
                        {
                            if (targetNames.contains(GeneralName.getInstance(targets[j]
                                                       .getTargetName())))
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found)
                    {
                        return false;
                    }
                }
                if (!targetGroups.isEmpty())
                {
                    boolean found = false;

                    for (int i=0; i<targetss.length; i++)
                    {
                        Targets t = targetss[i];
                        Target[] targets = t.getTargets();
                        for (int j=0; j<targets.length; j++)
                        {
                            if (targetGroups.contains(GeneralName.getInstance(targets[j]
                                                        .getTargetGroup())))
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns a clone of this object.
     *
     * @return the clone.
     */
    public Object clone()
    {
        X509AttributeCertificateSelector sel = new X509AttributeCertificateSelector(
            holder, issuer, serialNumber, attributeCertificateValid, attributeCert, targetNames, targetGroups);

        return sel;
    }

    /**
     * Returns the attribute certificate holder which must be matched.
     *
     * @return Returns an X509AttributeCertificateHolder
     */
    public X509AttributeCertificateHolder getAttributeCert()
    {
        return attributeCert;
    }

    /**
     * Get the criteria for the validity.
     *
     * @return Returns the attributeCertificateValid.
     */
    public Date getAttributeCertificateValid()
    {
        if (attributeCertificateValid != null)
        {
            return new Date(attributeCertificateValid.getTime());
        }

        return null;
    }

    /**
     * Gets the holder.
     *
     * @return Returns the holder.
     */
    public AttributeCertificateHolder getHolder()
    {
        return holder;
    }

    /**
     * Returns the issuer criterion.
     *
     * @return Returns the issuer.
     */
    public AttributeCertificateIssuer getIssuer()
    {
        return issuer;
    }

    /**
     * Gets the serial number the attribute certificate must have.
     *
     * @return Returns the serialNumber.
     */
    public BigInteger getSerialNumber()
    {
        return serialNumber;
    }

    /**
     * Gets the target names. The collection consists of GeneralName objects.
     * <p>
     * The returned collection is immutable.
     *
     * @return The collection of target names
     */
    public Collection getTargetNames()
    {
        return targetNames;
    }

    /**
     * Gets the target groups. The collection consists of GeneralName objects.
     * <p>
     * The returned collection is immutable.
     *
     * @return The collection of target groups.
     */
    public Collection getTargetGroups()
    {
        return targetGroups;
    }
}
