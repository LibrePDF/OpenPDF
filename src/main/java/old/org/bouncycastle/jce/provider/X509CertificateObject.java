package old.org.bouncycastle.jce.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import old.org.bouncycastle.asn1.misc.NetscapeCertType;
import old.org.bouncycastle.asn1.misc.NetscapeRevocationURL;
import old.org.bouncycastle.asn1.misc.VerisignCzagExtension;
import old.org.bouncycastle.asn1.util.ASN1Dump;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.BasicConstraints;
import old.org.bouncycastle.asn1.x509.KeyUsage;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.X509Principal;
import old.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import old.org.bouncycastle.util.Arrays;
import old.org.bouncycastle.util.encoders.Hex;

public class X509CertificateObject
    extends X509Certificate
    implements PKCS12BagAttributeCarrier
{
    private X509CertificateStructure    c;
    private BasicConstraints            basicConstraints;
    private boolean[]                   keyUsage;
    private boolean                     hashValueSet;
    private int                         hashValue;

    private PKCS12BagAttributeCarrier   attrCarrier = new PKCS12BagAttributeCarrierImpl();

    public X509CertificateObject(
        X509CertificateStructure    c)
        throws CertificateParsingException
    {
        this.c = c;

        try
        {
            byte[]  bytes = this.getExtensionBytes("2.5.29.19");

            if (bytes != null)
            {
                basicConstraints = BasicConstraints.getInstance(ASN1Object.fromByteArray(bytes));
            }
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("cannot construct BasicConstraints: " + e);
        }

        try
        {
            byte[] bytes = this.getExtensionBytes("2.5.29.15");
            if (bytes != null)
            {
                DERBitString    bits = DERBitString.getInstance(ASN1Object.fromByteArray(bytes));

                bytes = bits.getBytes();
                int length = (bytes.length * 8) - bits.getPadBits();

                keyUsage = new boolean[(length < 9) ? 9 : length];

                for (int i = 0; i != length; i++)
                {
                    keyUsage[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
                }
            }
            else
            {
                keyUsage = null;
            }
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("cannot construct KeyUsage: " + e);
        }
    }

    public void checkValidity()
        throws CertificateExpiredException, CertificateNotYetValidException
    {
        this.checkValidity(new Date());
    }

    public void checkValidity(
        Date    date)
        throws CertificateExpiredException, CertificateNotYetValidException
    {
        if (date.getTime() > this.getNotAfter().getTime())  // for other VM compatibility
        {
            throw new CertificateExpiredException("certificate expired on " + c.getEndDate().getTime());
        }

        if (date.getTime() < this.getNotBefore().getTime())
        {
            throw new CertificateNotYetValidException("certificate not valid till " + c.getStartDate().getTime());
        }
    }

    public int getVersion()
    {
        return c.getVersion();
    }

    public BigInteger getSerialNumber()
    {
        return c.getSerialNumber().getValue();
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

    public Principal getSubjectDN()
    {
        return new X509Principal(c.getSubject());
    }

    public X500Principal getSubjectX500Principal()
    {
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

            aOut.writeObject(c.getSubject());

            return new X500Principal(bOut.toByteArray());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("can't encode issuer DN");
        }
    }

    public Date getNotBefore()
    {
        return c.getStartDate().getDate();
    }

    public Date getNotAfter()
    {
        return c.getEndDate().getDate();
    }

    public byte[] getTBSCertificate()
        throws CertificateEncodingException
    {
        try
        {
            return c.getTBSCertificate().getEncoded(ASN1Encodable.DER);
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public byte[] getSignature()
    {
        return c.getSignature().getBytes();
    }

    /**
     * return a more "meaningful" representation for the signature algorithm used in
     * the certficate.
     */
    public String getSigAlgName()
    {
        Provider    prov = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);

        if (prov != null)
        {
            String      algName = prov.getProperty("Alg.Alias.Signature." + this.getSigAlgOID());

            if (algName != null)
            {
                return algName;
            }
        }

        Provider[] provs = Security.getProviders();

        //
        // search every provider looking for a real algorithm
        //
        for (int i = 0; i != provs.length; i++)
        {
            String algName = provs[i].getProperty("Alg.Alias.Signature." + this.getSigAlgOID());
            if (algName != null)
            {
                return algName;
            }
        }

        return this.getSigAlgOID();
    }

    /**
     * return the object identifier for the signature.
     */
    public String getSigAlgOID()
    {
        return c.getSignatureAlgorithm().getObjectId().getId();
    }

    /**
     * return the signature parameters, or null if there aren't any.
     */
    public byte[] getSigAlgParams()
    {
        if (c.getSignatureAlgorithm().getParameters() != null)
        {
            return c.getSignatureAlgorithm().getParameters().getDERObject().getDEREncoded();
        }
        else
        {
            return null;
        }
    }

    public boolean[] getIssuerUniqueID()
    {
        DERBitString    id = c.getTBSCertificate().getIssuerUniqueId();

        if (id != null)
        {
            byte[]          bytes = id.getBytes();
            boolean[]       boolId = new boolean[bytes.length * 8 - id.getPadBits()];

            for (int i = 0; i != boolId.length; i++)
            {
                boolId[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
            }

            return boolId;
        }
            
        return null;
    }

    public boolean[] getSubjectUniqueID()
    {
        DERBitString    id = c.getTBSCertificate().getSubjectUniqueId();

        if (id != null)
        {
            byte[]          bytes = id.getBytes();
            boolean[]       boolId = new boolean[bytes.length * 8 - id.getPadBits()];

            for (int i = 0; i != boolId.length; i++)
            {
                boolId[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
            }

            return boolId;
        }
            
        return null;
    }

    public boolean[] getKeyUsage()
    {
        return keyUsage;
    }

    public List getExtendedKeyUsage() 
        throws CertificateParsingException
    {
        byte[]  bytes = this.getExtensionBytes("2.5.29.37");

        if (bytes != null)
        {
            try
            {
                ASN1InputStream dIn = new ASN1InputStream(bytes);
                ASN1Sequence    seq = (ASN1Sequence)dIn.readObject();
                List            list = new ArrayList();

                for (int i = 0; i != seq.size(); i++)
                {
                    list.add(((DERObjectIdentifier)seq.getObjectAt(i)).getId());
                }
                
                return Collections.unmodifiableList(list);
            }
            catch (Exception e)
            {
                throw new CertificateParsingException("error processing extended key usage extension");
            }
        }

        return null;
    }
    
    public int getBasicConstraints()
    {
        if (basicConstraints != null)
        {
            if (basicConstraints.isCA())
            {
                if (basicConstraints.getPathLenConstraint() == null)
                {
                    return Integer.MAX_VALUE;
                }
                else
                {
                    return basicConstraints.getPathLenConstraint().intValue();
                }
            }
            else
            {
                return -1;
            }
        }

        return -1;
    }

    public Set getCriticalExtensionOIDs() 
    {
        if (this.getVersion() == 3)
        {
            Set             set = new HashSet();
            X509Extensions  extensions = c.getTBSCertificate().getExtensions();

            if (extensions != null)
            {
                Enumeration     e = extensions.oids();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension       ext = extensions.getExtension(oid);

                    if (ext.isCritical())
                    {
                        set.add(oid.getId());
                    }
                }

                return set;
            }
        }

        return null;
    }

    private byte[] getExtensionBytes(String oid)
    {
        X509Extensions exts = c.getTBSCertificate().getExtensions();

        if (exts != null)
        {
            X509Extension   ext = exts.getExtension(new DERObjectIdentifier(oid));
            if (ext != null)
            {
                return ext.getValue().getOctets();
            }
        }

        return null;
    }

    public byte[] getExtensionValue(String oid) 
    {
        X509Extensions exts = c.getTBSCertificate().getExtensions();

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

    public Set getNonCriticalExtensionOIDs() 
    {
        if (this.getVersion() == 3)
        {
            Set             set = new HashSet();
            X509Extensions  extensions = c.getTBSCertificate().getExtensions();

            if (extensions != null)
            {
                Enumeration     e = extensions.oids();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    X509Extension       ext = extensions.getExtension(oid);

                    if (!ext.isCritical())
                    {
                        set.add(oid.getId());
                    }
                }

                return set;
            }
        }

        return null;
    }

    public boolean hasUnsupportedCriticalExtension()
    {
        if (this.getVersion() == 3)
        {
            X509Extensions  extensions = c.getTBSCertificate().getExtensions();

            if (extensions != null)
            {
                Enumeration     e = extensions.oids();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    String              oidId = oid.getId();

                    if (oidId.equals(RFC3280CertPathUtilities.KEY_USAGE)
                     || oidId.equals(RFC3280CertPathUtilities.CERTIFICATE_POLICIES)
                     || oidId.equals(RFC3280CertPathUtilities.POLICY_MAPPINGS)
                     || oidId.equals(RFC3280CertPathUtilities.INHIBIT_ANY_POLICY)
                     || oidId.equals(RFC3280CertPathUtilities.CRL_DISTRIBUTION_POINTS)
                     || oidId.equals(RFC3280CertPathUtilities.ISSUING_DISTRIBUTION_POINT)
                     || oidId.equals(RFC3280CertPathUtilities.DELTA_CRL_INDICATOR)
                     || oidId.equals(RFC3280CertPathUtilities.POLICY_CONSTRAINTS)
                     || oidId.equals(RFC3280CertPathUtilities.BASIC_CONSTRAINTS)
                     || oidId.equals(RFC3280CertPathUtilities.SUBJECT_ALTERNATIVE_NAME)
                     || oidId.equals(RFC3280CertPathUtilities.NAME_CONSTRAINTS))
                    {
                        continue;
                    }

                    X509Extension       ext = extensions.getExtension(oid);

                    if (ext.isCritical())
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public PublicKey getPublicKey()
    {
        return JDKKeyFactory.createPublicKeyFromPublicKeyInfo(c.getSubjectPublicKeyInfo());
    }

    public byte[] getEncoded()
        throws CertificateEncodingException
    {
        try
        {
            return c.getEncoded(ASN1Encodable.DER);
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }

    public boolean equals(
        Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof Certificate))
        {
            return false;
        }

        Certificate other = (Certificate)o;

        try
        {
            byte[] b1 = this.getEncoded();
            byte[] b2 = other.getEncoded();

            return Arrays.areEqual(b1, b2);
        }
        catch (CertificateEncodingException e)
        {
            return false;
        }
    }
    
    public synchronized int hashCode()
    {
        if (!hashValueSet)
        {
            hashValue = calculateHashCode();
            hashValueSet = true;
        }

        return hashValue;
    }
    
    private int calculateHashCode()
    {
        try
        {
            int hashCode = 0;
            byte[] certData = this.getEncoded();
            for (int i = 1; i < certData.length; i++)
            {
                 hashCode += certData[i] * i;
            }
            return hashCode;
        }
        catch (CertificateEncodingException e)
        {
            return 0;
        }
    }

    public void setBagAttribute(
        DERObjectIdentifier oid,
        DEREncodable        attribute)
    {
        attrCarrier.setBagAttribute(oid, attribute);
    }

    public DEREncodable getBagAttribute(
        DERObjectIdentifier oid)
    {
        return attrCarrier.getBagAttribute(oid);
    }

    public Enumeration getBagAttributeKeys()
    {
        return attrCarrier.getBagAttributeKeys();
    }

    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");

        buf.append("  [0]         Version: ").append(this.getVersion()).append(nl);
        buf.append("         SerialNumber: ").append(this.getSerialNumber()).append(nl);
        buf.append("             IssuerDN: ").append(this.getIssuerDN()).append(nl);
        buf.append("           Start Date: ").append(this.getNotBefore()).append(nl);
        buf.append("           Final Date: ").append(this.getNotAfter()).append(nl);
        buf.append("            SubjectDN: ").append(this.getSubjectDN()).append(nl);
        buf.append("           Public Key: ").append(this.getPublicKey()).append(nl);
        buf.append("  Signature Algorithm: ").append(this.getSigAlgName()).append(nl);

        byte[]  sig = this.getSignature();

        buf.append("            Signature: ").append(new String(Hex.encode(sig, 0, 20))).append(nl);
        for (int i = 20; i < sig.length; i += 20)
        {
            if (i < sig.length - 20)
            {
                buf.append("                       ").append(new String(Hex.encode(sig, i, 20))).append(nl);
            }
            else
            {
                buf.append("                       ").append(new String(Hex.encode(sig, i, sig.length - i))).append(nl);
            }
        }

        X509Extensions  extensions = c.getTBSCertificate().getExtensions();

        if (extensions != null)
        {
            Enumeration     e = extensions.oids();

            if (e.hasMoreElements())
            {
                buf.append("       Extensions: \n");
            }

            while (e.hasMoreElements())
            {
                DERObjectIdentifier     oid = (DERObjectIdentifier)e.nextElement();
                X509Extension           ext = extensions.getExtension(oid);

                if (ext.getValue() != null)
                {
                    byte[]                  octs = ext.getValue().getOctets();
                    ASN1InputStream         dIn = new ASN1InputStream(octs);
                    buf.append("                       critical(").append(ext.isCritical()).append(") ");
                    try
                    {
                        if (oid.equals(X509Extensions.BasicConstraints))
                        {
                            buf.append(new BasicConstraints((ASN1Sequence)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(X509Extensions.KeyUsage))
                        {
                            buf.append(new KeyUsage((DERBitString)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(MiscObjectIdentifiers.netscapeCertType))
                        {
                            buf.append(new NetscapeCertType((DERBitString)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(MiscObjectIdentifiers.netscapeRevocationURL))
                        {
                            buf.append(new NetscapeRevocationURL((DERIA5String)dIn.readObject())).append(nl);
                        }
                        else if (oid.equals(MiscObjectIdentifiers.verisignCzagExtension))
                        {
                            buf.append(new VerisignCzagExtension((DERIA5String)dIn.readObject())).append(nl);
                        }
                        else 
                        {
                            buf.append(oid.getId());
                            buf.append(" value = ").append(ASN1Dump.dumpAsString(dIn.readObject())).append(nl);
                            //buf.append(" value = ").append("*****").append(nl);
                        }
                    }
                    catch (Exception ex)
                    {
                        buf.append(oid.getId());
                   //     buf.append(" value = ").append(new String(Hex.encode(ext.getValue().getOctets()))).append(nl);
                        buf.append(" value = ").append("*****").append(nl);
                    }
                }
                else
                {
                    buf.append(nl);
                }
            }
        }

        return buf.toString();
    }

    public final void verify(
        PublicKey   key)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException, SignatureException
    {
        Signature   signature;
        String      sigName = X509SignatureUtil.getSignatureName(c.getSignatureAlgorithm());
        
        try
        {
            signature = Signature.getInstance(sigName, BouncyCastleProvider.PROVIDER_NAME);
        }
        catch (Exception e)
        {
            signature = Signature.getInstance(sigName);
        }
        
        checkSignature(key, signature);
    }
    
    public final void verify(
        PublicKey   key,
        String      sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException, SignatureException
    {
        String    sigName = X509SignatureUtil.getSignatureName(c.getSignatureAlgorithm());
        Signature signature = Signature.getInstance(sigName, sigProvider);
        
        checkSignature(key, signature);
    }

    private void checkSignature(
        PublicKey key, 
        Signature signature) 
        throws CertificateException, NoSuchAlgorithmException, 
            SignatureException, InvalidKeyException
    {
        if (!isAlgIdEqual(c.getSignatureAlgorithm(), c.getTBSCertificate().getSignature()))
        {
            throw new CertificateException("signature algorithm in TBS cert not same as outer cert");
        }

        DEREncodable params = c.getSignatureAlgorithm().getParameters();

        // TODO This should go after the initVerify?
        X509SignatureUtil.setSignatureParameters(signature, params);

        signature.initVerify(key);

        signature.update(this.getTBSCertificate());

        if (!signature.verify(this.getSignature()))
        {
            throw new InvalidKeyException("Public key presented not for certificate signature");
        }
    }

    private boolean isAlgIdEqual(AlgorithmIdentifier id1, AlgorithmIdentifier id2)
    {
        if (!id1.getObjectId().equals(id2.getObjectId()))
        {
            return false;
        }

        if (id1.getParameters() == null)
        {
            if (id2.getParameters() != null && !id2.getParameters().equals(DERNull.INSTANCE))
            {
                return false;
            }

            return true;
        }

        if (id2.getParameters() == null)
        {
            if (id1.getParameters() != null && !id1.getParameters().equals(DERNull.INSTANCE))
            {
                return false;
            }

            return true;
        }
        
        return id1.getParameters().equals(id2.getParameters());
    }
}
