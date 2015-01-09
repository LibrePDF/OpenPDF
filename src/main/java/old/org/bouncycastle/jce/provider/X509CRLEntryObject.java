package old.org.bouncycastle.jce.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.X509CRLEntry;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREnumerated;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.util.ASN1Dump;
import old.org.bouncycastle.asn1.x509.CRLReason;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.GeneralNames;
import old.org.bouncycastle.asn1.x509.TBSCertList;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 * The following extensions are listed in RFC 2459 as relevant to CRL Entries
 * 
 * ReasonCode Hode Instruction Code Invalidity Date Certificate Issuer
 * (critical)
 */
public class X509CRLEntryObject extends X509CRLEntry
{
    private TBSCertList.CRLEntry c;

    private boolean isIndirect;

    private X500Principal previousCertificateIssuer;
    private X500Principal certificateIssuer;
    private int           hashValue;
    private boolean       isHashValueSet;

    public X509CRLEntryObject(TBSCertList.CRLEntry c)
    {
        this.c = c;
        this.certificateIssuer = loadCertificateIssuer();
    }

    /**
     * Constructor for CRLEntries of indirect CRLs. If <code>isIndirect</code>
     * is <code>false</code> {@link #getCertificateIssuer()} will always
     * return <code>null</code>, <code>previousCertificateIssuer</code> is
     * ignored. If this <code>isIndirect</code> is specified and this CRLEntry
     * has no certificate issuer CRL entry extension
     * <code>previousCertificateIssuer</code> is returned by
     * {@link #getCertificateIssuer()}.
     * 
     * @param c
     *            TBSCertList.CRLEntry object.
     * @param isIndirect
     *            <code>true</code> if the corresponding CRL is a indirect
     *            CRL.
     * @param previousCertificateIssuer
     *            Certificate issuer of the previous CRLEntry.
     */
    public X509CRLEntryObject(
        TBSCertList.CRLEntry c,
        boolean isIndirect,
        X500Principal previousCertificateIssuer)
    {
        this.c = c;
        this.isIndirect = isIndirect;
        this.previousCertificateIssuer = previousCertificateIssuer;
        this.certificateIssuer = loadCertificateIssuer();
    }

    /**
     * Will return true if any extensions are present and marked as critical as
     * we currently dont handle any extensions!
     */
    public boolean hasUnsupportedCriticalExtension()
    {
        Set extns = getCriticalExtensionOIDs();

        return extns != null && !extns.isEmpty();
    }

    private X500Principal loadCertificateIssuer()
    {
        if (!isIndirect)
        {
            return null;
        }

        byte[] ext = getExtensionValue(X509Extensions.CertificateIssuer.getId());
        if (ext == null)
        {
            return previousCertificateIssuer;
        }

        try
        {
            GeneralName[] names = GeneralNames.getInstance(
                    X509ExtensionUtil.fromExtensionValue(ext)).getNames();
            for (int i = 0; i < names.length; i++)
            {
                if (names[i].getTagNo() == GeneralName.directoryName)
                {
                    return new X500Principal(names[i].getName().getDERObject().getDEREncoded());
                }
            }
            return null;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public X500Principal getCertificateIssuer()
    {
        return certificateIssuer;
    }

    private Set getExtensionOIDs(boolean critical)
    {
        X509Extensions extensions = c.getExtensions();

        if (extensions != null)
        {
            Set set = new HashSet();
            Enumeration e = extensions.oids();

            while (e.hasMoreElements())
            {
                DERObjectIdentifier oid = (DERObjectIdentifier) e.nextElement();
                X509Extension ext = extensions.getExtension(oid);

                if (critical == ext.isCritical())
                {
                    set.add(oid.getId());
                }
            }

            return set;
        }

        return null;
    }

    public Set getCriticalExtensionOIDs()
    {
        return getExtensionOIDs(true);
    }

    public Set getNonCriticalExtensionOIDs()
    {
        return getExtensionOIDs(false);
    }

    public byte[] getExtensionValue(String oid)
    {
        X509Extensions exts = c.getExtensions();

        if (exts != null)
        {
            X509Extension ext = exts.getExtension(new DERObjectIdentifier(oid));

            if (ext != null)
            {
                try
                {
                    return ext.getValue().getEncoded();
                }
                catch (Exception e)
                {
                    throw new RuntimeException("error encoding " + e.toString());
                }
            }
        }

        return null;
    }

    /**
     * Cache the hashCode value - calculating it with the standard method.
     * @return  calculated hashCode.
     */
    public int hashCode()
    {
        if (!isHashValueSet)
        {
            hashValue = super.hashCode();
            isHashValueSet = true;
        }

        return hashValue;
    }

    public byte[] getEncoded()
        throws CRLException
    {
        try
        {
            return c.getEncoded(ASN1Encodable.DER);
        }
        catch (IOException e)
        {
            throw new CRLException(e.toString());
        }
    }

    public BigInteger getSerialNumber()
    {
        return c.getUserCertificate().getValue();
    }

    public Date getRevocationDate()
    {
        return c.getRevocationDate().getDate();
    }

    public boolean hasExtensions()
    {
        return c.getExtensions() != null;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        String nl = System.getProperty("line.separator");

        buf.append("      userCertificate: ").append(this.getSerialNumber()).append(nl);
        buf.append("       revocationDate: ").append(this.getRevocationDate()).append(nl);
        buf.append("       certificateIssuer: ").append(this.getCertificateIssuer()).append(nl);

        X509Extensions extensions = c.getExtensions();

        if (extensions != null)
        {
            Enumeration e = extensions.oids();
            if (e.hasMoreElements())
            {
                buf.append("   crlEntryExtensions:").append(nl);

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension ext = extensions.getExtension(oid);
                    if (ext.getValue() != null)
                    {
                        byte[]                  octs = ext.getValue().getOctets();
                        ASN1InputStream dIn = new ASN1InputStream(octs);
                        buf.append("                       critical(").append(ext.isCritical()).append(") ");
                        try
                        {
                            if (oid.equals(X509Extensions.ReasonCode))
                            {
                                buf.append(new CRLReason(DEREnumerated.getInstance(dIn.readObject()))).append(nl);
                            }
                            else if (oid.equals(X509Extensions.CertificateIssuer))
                            {
                                buf.append("Certificate issuer: ").append(new GeneralNames((ASN1Sequence)dIn.readObject())).append(nl);
                            }
                            else 
                            {
                                buf.append(oid.getId());
                                buf.append(" value = ").append(ASN1Dump.dumpAsString(dIn.readObject())).append(nl);
                            }
                        }
                        catch (Exception ex)
                        {
                            buf.append(oid.getId());
                            buf.append(" value = ").append("*****").append(nl);
                        }
                    }
                    else
                    {
                        buf.append(nl);
                    }
                }
            }
        }

        return buf.toString();
    }
}
