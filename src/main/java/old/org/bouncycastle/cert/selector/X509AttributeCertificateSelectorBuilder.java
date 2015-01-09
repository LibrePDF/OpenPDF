package old.org.bouncycastle.cert.selector;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.cert.AttributeCertificateHolder;
import old.org.bouncycastle.cert.AttributeCertificateIssuer;
import old.org.bouncycastle.cert.X509AttributeCertificateHolder;

/**
 * This class builds selectors according to the set criteria.
 */
public class X509AttributeCertificateSelectorBuilder
{

    // TODO: name constraints???

    private AttributeCertificateHolder holder;

    private AttributeCertificateIssuer issuer;

    private BigInteger serialNumber;

    private Date attributeCertificateValid;

    private X509AttributeCertificateHolder attributeCert;

    private Collection targetNames = new HashSet();

    private Collection targetGroups = new HashSet();

    public X509AttributeCertificateSelectorBuilder()
    {
    }

    /**
     * Set the attribute certificate to be matched. If <code>null</code> is
     * given any will do.
     *
     * @param attributeCert The attribute certificate holder to set.
     */
    public void setAttributeCert(X509AttributeCertificateHolder attributeCert)
    {
        this.attributeCert = attributeCert;
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
     * Sets the holder. If <code>null</code> is given any will do.
     *
     * @param holder The holder to set.
     */
    public void setHolder(AttributeCertificateHolder holder)
    {
        this.holder = holder;
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
     * information extension criteria. The <code>X509AttributeCertificateHolder</code>
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
     * Adds a collection with target names criteria. If <code>null</code> is
     * given any will do.
     * <p>
     * The collection consists of either GeneralName objects or byte[] arrays representing
     * DER encoded GeneralName structures.
     *
     * @param names A collection of target names.
     * @throws java.io.IOException if a parsing error occurs.
     * @see #addTargetName(old.org.bouncycastle.asn1.x509.GeneralName)
     */
    public void setTargetNames(Collection names) throws IOException
    {
        targetNames = extractGeneralNames(names);
    }

    /**
     * Adds a target group criterion for the attribute certificate to the target
     * information extension criteria. The <code>X509AttributeCertificateHolder</code>
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
     * Adds a collection with target groups criteria. If <code>null</code> is
     * given any will do.
     * <p>
     * The collection consists of <code>GeneralName</code> objects or <code>byte[]</code representing DER
     * encoded GeneralNames.
     *
     * @param names A collection of target groups.
     * @throws java.io.IOException if a parsing error occurs.
     * @see #addTargetGroup(old.org.bouncycastle.asn1.x509.GeneralName)
     */
    public void setTargetGroups(Collection names) throws IOException
    {
        targetGroups = extractGeneralNames(names);
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
            temp.add(GeneralName.getInstance(it.next()));
        }
        return temp;
    }

    public X509AttributeCertificateSelector build()
    {
        X509AttributeCertificateSelector sel = new X509AttributeCertificateSelector(
            holder, issuer, serialNumber, attributeCertificateValid, attributeCert, Collections.unmodifiableCollection(new HashSet(targetNames)), Collections.unmodifiableCollection(new HashSet(targetGroups)));

        return sel;
    }
}
