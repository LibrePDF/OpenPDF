package old.org.bouncycastle.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.Target;
import old.org.bouncycastle.asn1.x509.TargetInformation;
import old.org.bouncycastle.asn1.x509.Targets;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.util.Selector;

/**
 * This class is an <code>Selector</code> like implementation to select
 * attribute certificates from a given set of criteria.
 * 
 * @see old.org.bouncycastle.x509.X509AttributeCertificate
 * @see old.org.bouncycastle.x509.X509Store
 *  @deprecated use org.bouncycastle.cert.X509AttributeCertificateSelector and org.bouncycastle.cert.X509AttributeCertificateSelectorBuilder.
 */
public class X509AttributeCertStoreSelector
    implements Selector
{

    // TODO: name constraints???

    private AttributeCertificateHolder holder;

    private AttributeCertificateIssuer issuer;

    private BigInteger serialNumber;

    private Date attributeCertificateValid;

    private X509AttributeCertificate attributeCert;

    private Collection targetNames = new HashSet();

    private Collection targetGroups = new HashSet();

    public X509AttributeCertStoreSelector()
    {
        super();
    }

    /**
     * Decides if the given attribute certificate should be selected.
     * 
     * @param obj The attribute certificate which should be checked.
     * @return <code>true</code> if the attribute certificate can be selected,
     *         <code>false</code> otherwise.
     */
    public boolean match(Object obj)
    {
        if (!(obj instanceof X509AttributeCertificate))
        {
            return false;
        }

        X509AttributeCertificate attrCert = (X509AttributeCertificate) obj;

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
            try
            {
                attrCert.checkValidity(attributeCertificateValid);
            }
            catch (CertificateExpiredException e)
            {
                return false;
            }
            catch (CertificateNotYetValidException e)
            {
                return false;
            }
        }
        if (!targetNames.isEmpty() || !targetGroups.isEmpty())
        {

            byte[] targetInfoExt = attrCert
                .getExtensionValue(X509Extensions.TargetInformation.getId());
            if (targetInfoExt != null)
            {
                TargetInformation targetinfo;
                try
                {
                    targetinfo = TargetInformation
                        .getInstance(new ASN1InputStream(
                            ((DEROctetString) DEROctetString
                                .fromByteArray(targetInfoExt)).getOctets())
                            .readObject());
                }
                catch (IOException e)
                {
                    return false;
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
        X509AttributeCertStoreSelector sel = new X509AttributeCertStoreSelector();
        sel.attributeCert = attributeCert;
        sel.attributeCertificateValid = getAttributeCertificateValid();
        sel.holder = holder;
        sel.issuer = issuer;
        sel.serialNumber = serialNumber;
        sel.targetGroups = getTargetGroups();
        sel.targetNames = getTargetNames();
        return sel;
    }

    /**
     * Returns the attribute certificate which must be matched.
     * 
     * @return Returns the attribute certificate.
     */
    public X509AttributeCertificate getAttributeCert()
    {
        return attributeCert;
    }

    /**
     * Set the attribute certificate to be matched. If <code>null</code> is
     * given any will do.
     * 
     * @param attributeCert The attribute certificate to set.
     */
    public void setAttributeCert(X509AttributeCertificate attributeCert)
    {
        this.attributeCert = attributeCert;
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
     * Set the time, when the certificate must be valid. If <code>null</code>
     * is given any will do.
     * 
     * @param attributeCertificateValid The attribute certificate validation
     *            time to set.
     */
    public void setAttributeCertificateValid(Date attributeCertificateValid)
    {
        if (attributeCertificateValid != null)
        {
            this.attributeCertificateValid = new Date(attributeCertificateValid
                .getTime());
        }
        else
        {
            this.attributeCertificateValid = null;
        }
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
     * Sets the holder. If <code>null</code> is given any will do.
     * 
     * @param holder The holder to set.
     */
    public void setHolder(AttributeCertificateHolder holder)
    {
        this.holder = holder;
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
     * Sets the issuer the attribute certificate must have. If <code>null</code>
     * is given any will do.
     * 
     * @param issuer The issuer to set.
     */
    public void setIssuer(AttributeCertificateIssuer issuer)
    {
        this.issuer = issuer;
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
     * Sets the serial number the attribute certificate must have. If
     * <code>null</code> is given any will do.
     * 
     * @param serialNumber The serialNumber to set.
     */
    public void setSerialNumber(BigInteger serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    /**
     * Adds a target name criterion for the attribute certificate to the target
     * information extension criteria. The <code>X509AttributeCertificate</code>
     * must contain at least one of the specified target names.
     * <p>
     * Each attribute certificate may contain a target information extension
     * limiting the servers where this attribute certificate can be used. If
     * this extension is not present, the attribute certificate is not targeted
     * and may be accepted by any server.
     *
     * @param name The name as a GeneralName (not <code>null</code>)
     */
    public void addTargetName(GeneralName name)
    {
        targetNames.add(name);
    }

    /**
     * Adds a target name criterion for the attribute certificate to the target
     * information extension criteria. The <code>X509AttributeCertificate</code>
     * must contain at least one of the specified target names.
     * <p>
     * Each attribute certificate may contain a target information extension
     * limiting the servers where this attribute certificate can be used. If
     * this extension is not present, the attribute certificate is not targeted
     * and may be accepted by any server.
     *
     * @param name a byte array containing the name in ASN.1 DER encoded form of a GeneralName
     * @throws IOException if a parsing error occurs.
     */
    public void addTargetName(byte[] name) throws IOException
    {
        addTargetName(GeneralName.getInstance(ASN1Object.fromByteArray(name)));
    }

    /**
     * Adds a collection with target names criteria. If <code>null</code> is
     * given any will do.
     * <p>
     * The collection consists of either GeneralName objects or byte[] arrays representing
     * DER encoded GeneralName structures.
     * 
     * @param names A collection of target names.
     * @throws IOException if a parsing error occurs.
     * @see #addTargetName(byte[])
     * @see #addTargetName(GeneralName)
     */
    public void setTargetNames(Collection names) throws IOException
    {
        targetNames = extractGeneralNames(names);
    }

    /**
     * Gets the target names. The collection consists of <code>GeneralName</code>
     * objects.
     * <p>
     * The returned collection is immutable.
     * 
     * @return The collection of target names
     * @see #setTargetNames(Collection)
     */
    public Collection getTargetNames()
    {
        return Collections.unmodifiableCollection(targetNames);
    }

    /**
     * Adds a target group criterion for the attribute certificate to the target
     * information extension criteria. The <code>X509AttributeCertificate</code>
     * must contain at least one of the specified target groups.
     * <p>
     * Each attribute certificate may contain a target information extension
     * limiting the servers where this attribute certificate can be used. If
     * this extension is not present, the attribute certificate is not targeted
     * and may be accepted by any server.
     *
     * @param group The group as GeneralName form (not <code>null</code>)
     */
    public void addTargetGroup(GeneralName group)
    {
        targetGroups.add(group);
    }

    /**
     * Adds a target group criterion for the attribute certificate to the target
     * information extension criteria. The <code>X509AttributeCertificate</code>
     * must contain at least one of the specified target groups.
     * <p>
     * Each attribute certificate may contain a target information extension
     * limiting the servers where this attribute certificate can be used. If
     * this extension is not present, the attribute certificate is not targeted
     * and may be accepted by any server.
     *
     * @param name a byte array containing the group in ASN.1 DER encoded form of a GeneralName
     * @throws IOException if a parsing error occurs.
     */
    public void addTargetGroup(byte[] name) throws IOException
    {
        addTargetGroup(GeneralName.getInstance(ASN1Object.fromByteArray(name)));
    }

    /**
     * Adds a collection with target groups criteria. If <code>null</code> is
     * given any will do.
     * <p>
     * The collection consists of <code>GeneralName</code> objects or <code>byte[]</code representing DER
     * encoded GeneralNames.
     * 
     * @param names A collection of target groups.
     * @throws IOException if a parsing error occurs.
     * @see #addTargetGroup(byte[])
     * @see #addTargetGroup(GeneralName)
     */
    public void setTargetGroups(Collection names) throws IOException
    {
        targetGroups = extractGeneralNames(names);
    }



    /**
     * Gets the target groups. The collection consists of <code>GeneralName</code> objects.
     * <p>
     * The returned collection is immutable.
     *
     * @return The collection of target groups.
     * @see #setTargetGroups(Collection)
     */
    public Collection getTargetGroups()
    {
        return Collections.unmodifiableCollection(targetGroups);
    }

    private Set extractGeneralNames(Collection names)
        throws IOException
    {
        if (names == null || names.isEmpty())
        {
            return new HashSet();
        }
        Set temp = new HashSet();
        for (Iterator it = names.iterator(); it.hasNext();)
        {
            Object o = it.next();
            if (o instanceof GeneralName)
            {
                temp.add(o);
            }
            else
            {
                temp.add(GeneralName.getInstance(ASN1Object.fromByteArray((byte[])o)));
            }
        }
        return temp;
    }
}
