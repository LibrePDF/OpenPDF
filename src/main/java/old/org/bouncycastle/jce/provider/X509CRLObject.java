package old.org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.util.ASN1Dump;
import old.org.bouncycastle.asn1.x509.CRLDistPoint;
import old.org.bouncycastle.asn1.x509.CRLNumber;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import old.org.bouncycastle.asn1.x509.TBSCertList;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.X509Principal;
import old.org.bouncycastle.util.encoders.Hex;
import old.org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 * The following extensions are listed in RFC 2459 as relevant to CRLs
 *
 * Authority Key Identifier
 * Issuer Alternative Name
 * CRL Number
 * Delta CRL Indicator (critical)
 * Issuing Distribution Point (critical)
 */
public class X509CRLObject
    extends X509CRL
{
    private CertificateList c;
    private String sigAlgName;
    private byte[] sigAlgParams;
    private boolean isIndirect;

    public X509CRLObject(
        CertificateList c)
        throws CRLException
    {
        this.c = c;
        
        try
        {
            this.sigAlgName = X509SignatureUtil.getSignatureName(c.getSignatureAlgorithm());
            
            if (c.getSignatureAlgorithm().getParameters() != null)
            {
                this.sigAlgParams = ((ASN1Encodable)c.getSignatureAlgorithm().getParameters()).getDEREncoded();
            }
            else
            {
                this.sigAlgParams = null;
            }

            this.isIndirect = isIndirectCRL();
        }
        catch (Exception e)
        {
            throw new CRLException("CRL contents invalid: " + e);
        }
    }

    /**
     * Will return true if any extensions are present and marked
     * as critical as we currently dont handle any extensions!
     */
    public boolean hasUnsupportedCriticalExtension()
    {
        Set extns = getCriticalExtensionOIDs();

        if (extns == null)
        {
            return false;
        }

        extns.remove(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT);
        extns.remove(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR);

        return !extns.isEmpty();
    }

    private Set getExtensionOIDs(boolean critical)
    {
        if (this.getVersion() == 2)
        {
            X509Extensions extensions = c.getTBSCertList().getExtensions();

            if (extensions != null)
            {
                Set set = new HashSet();
                Enumeration e = extensions.oids();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension ext = extensions.getExtension(oid);

                    if (critical == ext.isCritical())
                    {
                        set.add(oid.getId());
                    }
                }

                return set;
            }
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
        X509Extensions exts = c.getTBSCertList().getExtensions();

        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new DERObjectIdentifier(oid));

            if (ext != null)
            {
                try
                {
                    return ext.getValue().getEncoded();
                }
                catch (Exception e)
                {
                    throw new IllegalStateException("error parsing " + e.toString());
                }
            }
        }

        return null;
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

    public void verify(PublicKey key)
        throws CRLException,  NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException
    {
        verify(key, BouncyCastleProvider.PROVIDER_NAME);
    }

    public void verify(PublicKey key, String sigProvider)
        throws CRLException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException
    {
        if (!c.getSignatureAlgorithm().equals(c.getTBSCertList().getSignature()))
        {
            throw new CRLException("Signature algorithm on CertificateList does not match TBSCertList.");
        }

        Signature sig = Signature.getInstance(getSigAlgName(), sigProvider);

        sig.initVerify(key);
        sig.update(this.getTBSCertList());
        if (!sig.verify(this.getSignature()))
        {
            throw new SignatureException("CRL does not verify with supplied public key.");
        }
    }

    public int getVersion()
    {
        return c.getVersion();
    }

    public Principal getIssuerDN()
    {
        return new X509Principal(c.getIssuer());
    }

    public X500Principal getIssuerX500Principal()
    {
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

            aOut.writeObject(c.getIssuer());

            return new X500Principal(bOut.toByteArray());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }

    public Date getThisUpdate()
    {
        return c.getThisUpdate().getDate();
    }

    public Date getNextUpdate()
    {
        if (c.getNextUpdate() != null)
        {
            return c.getNextUpdate().getDate();
        }

        return null;
    }
 
    private Set loadCRLEntries()
    {
        Set entrySet = new HashSet();
        Enumeration certs = c.getRevokedCertificateEnumeration();

        X500Principal previousCertificateIssuer = getIssuerX500Principal();
        while (certs.hasMoreElements())
        {
            TBSCertList.CRLEntry entry = (TBSCertList.CRLEntry)certs.nextElement();
            X509CRLEntryObject crlEntry = new X509CRLEntryObject(entry, isIndirect, previousCertificateIssuer);
            entrySet.add(crlEntry);
            previousCertificateIssuer = crlEntry.getCertificateIssuer();
        }

        return entrySet;
    }

    public X509CRLEntry getRevokedCertificate(BigInteger serialNumber)
    {
        Enumeration certs = c.getRevokedCertificateEnumeration();

        X500Principal previousCertificateIssuer = getIssuerX500Principal();
        while (certs.hasMoreElements())
        {
            TBSCertList.CRLEntry entry = (TBSCertList.CRLEntry)certs.nextElement();
            X509CRLEntryObject crlEntry = new X509CRLEntryObject(entry, isIndirect, previousCertificateIssuer);

            if (serialNumber.equals(entry.getUserCertificate().getValue()))
            {
                return crlEntry;
            }

            previousCertificateIssuer = crlEntry.getCertificateIssuer();
        }

        return null;
    }

    public Set getRevokedCertificates()
    {
        Set entrySet = loadCRLEntries();

        if (!entrySet.isEmpty())
        {
            return Collections.unmodifiableSet(entrySet);
        }

        return null;
    }

    public byte[] getTBSCertList()
        throws CRLException
    {
        try
        {
            return c.getTBSCertList().getEncoded("DER");
        }
        catch (IOException e)
        {
            throw new CRLException(e.toString());
        }
    }

    public byte[] getSignature()
    {
        return c.getSignature().getBytes();
    }

    public String getSigAlgName()
    {
        return sigAlgName;
    }

    public String getSigAlgOID()
    {
        return c.getSignatureAlgorithm().getObjectId().getId();
    }

    public byte[] getSigAlgParams()
    {
        if (sigAlgParams != null)
        {
            byte[] tmp = new byte[sigAlgParams.length];
            
            System.arraycopy(sigAlgParams, 0, tmp, 0, tmp.length);
            
            return tmp;
        }
        
        return null;
    }

    /**
     * Returns a string representation of this CRL.
     *
     * @return a string representation of this CRL.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        String nl = System.getProperty("line.separator");

        buf.append("              Version: ").append(this.getVersion()).append(
            nl);
        buf.append("             IssuerDN: ").append(this.getIssuerDN())
            .append(nl);
        buf.append("          This update: ").append(this.getThisUpdate())
            .append(nl);
        buf.append("          Next update: ").append(this.getNextUpdate())
            .append(nl);
        buf.append("  Signature Algorithm: ").append(this.getSigAlgName())
            .append(nl);

        byte[] sig = this.getSignature();

        buf.append("            Signature: ").append(
            new String(Hex.encode(sig, 0, 20))).append(nl);
        for (int i = 20; i < sig.length; i += 20)
        {
            if (i < sig.length - 20)
            {
                buf.append("                       ").append(
                    new String(Hex.encode(sig, i, 20))).append(nl);
            }
            else
            {
                buf.append("                       ").append(
                    new String(Hex.encode(sig, i, sig.length - i))).append(nl);
            }
        }

        X509Extensions extensions = c.getTBSCertList().getExtensions();

        if (extensions != null)
        {
            Enumeration e = extensions.oids();

            if (e.hasMoreElements())
            {
                buf.append("           Extensions: ").append(nl);
            }

            while (e.hasMoreElements())
            {
                DERObjectIdentifier oid = (DERObjectIdentifier) e.nextElement();
                X509Extension ext = extensions.getExtension(oid);

                if (ext.getValue() != null)
                {
                    byte[] octs = ext.getValue().getOctets();
                    ASN1InputStream dIn = new ASN1InputStream(octs);
                    buf.append("                       critical(").append(
                        ext.isCritical()).append(") ");
                    try
                    {
                        if (oid.equals(X509Extensions.CRLNumber))
                        {
                            buf.append(
                                new CRLNumber(DERInteger.getInstance(
                                    dIn.readObject()).getPositiveValue()))
                                .append(nl);
                        }
                        else if (oid.equals(X509Extensions.DeltaCRLIndicator))
                        {
                            buf.append(
                                "Base CRL: "
                                    + new CRLNumber(DERInteger.getInstance(
                                        dIn.readObject()).getPositiveValue()))
                                .append(nl);
                        }
                        else if (oid
                            .equals(X509Extensions.IssuingDistributionPoint))
                        {
                            buf.append(
                                new IssuingDistributionPoint((ASN1Sequence) dIn
                                    .readObject())).append(nl);
                        }
                        else if (oid
                            .equals(X509Extensions.CRLDistributionPoints))
                        {
                            buf.append(
                                new CRLDistPoint((ASN1Sequence) dIn
                                    .readObject())).append(nl);
                        }
                        else if (oid.equals(X509Extensions.FreshestCRL))
                        {
                            buf.append(
                                new CRLDistPoint((ASN1Sequence) dIn
                                    .readObject())).append(nl);
                        }
                        else
                        {
                            buf.append(oid.getId());
                            buf.append(" value = ").append(
                                ASN1Dump.dumpAsString(dIn.readObject()))
                                .append(nl);
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
        Set set = getRevokedCertificates();
        if (set != null)
        {
            Iterator it = set.iterator();
            while (it.hasNext())
            {
                buf.append(it.next());
                buf.append(nl);
            }
        }
        return buf.toString();
    }

    /**
     * Checks whether the given certificate is on this CRL.
     *
     * @param cert the certificate to check for.
     * @return true if the given certificate is on this CRL,
     * false otherwise.
     */
    public boolean isRevoked(Certificate cert)
    {
        if (!cert.getType().equals("X.509"))
        {
            throw new RuntimeException("X.509 CRL used with non X.509 Cert");
        }

        TBSCertList.CRLEntry[] certs = c.getRevokedCertificates();

        if (certs != null)
        {
            BigInteger serial = ((X509Certificate)cert).getSerialNumber();

            for (int i = 0; i < certs.length; i++)
            {
                if (certs[i].getUserCertificate().getValue().equals(serial))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isIndirectCRL()
        throws CRLException
    {
        byte[] idp = getExtensionValue(X509Extensions.IssuingDistributionPoint.getId());
        boolean isIndirect = false;
        try
        {
            if (idp != null)
            {
                isIndirect = IssuingDistributionPoint.getInstance(
                        X509ExtensionUtil.fromExtensionValue(idp))
                        .isIndirectCRL();
            }
        }
        catch (Exception e)
        {
            throw new ExtCRLException(
                    "Exception reading IssuingDistributionPoint", e);
        }

        return isIndirect;
    }
}

