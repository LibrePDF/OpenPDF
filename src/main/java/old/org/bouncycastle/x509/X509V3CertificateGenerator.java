package old.org.bouncycastle.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.TBSCertificateStructure;
import old.org.bouncycastle.asn1.x509.Time;
import old.org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
import old.org.bouncycastle.asn1.x509.X509Name;
import old.org.bouncycastle.jce.X509Principal;
import old.org.bouncycastle.jce.provider.X509CertificateObject;
import old.org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 * class to produce an X.509 Version 3 certificate.
 *  @deprecated use org.bouncycastle.cert.X509v3CertificateBuilder.
 */
public class X509V3CertificateGenerator
{
    private V3TBSCertificateGenerator   tbsGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private X509ExtensionsGenerator     extGenerator;

    public X509V3CertificateGenerator()
    {
        tbsGen = new V3TBSCertificateGenerator();
        extGenerator = new X509ExtensionsGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        tbsGen = new V3TBSCertificateGenerator();
        extGenerator.reset();
    }

    /**
     * set the serial number for the certificate.
     */
    public void setSerialNumber(
        BigInteger      serialNumber)
    {
        if (serialNumber.compareTo(BigInteger.ZERO) <= 0)
        {
            throw new IllegalArgumentException("serial number must be a positive integer");
        }
        
        tbsGen.setSerialNumber(new DERInteger(serialNumber));
    }

    /**
     * Set the issuer distinguished name - the issuer is the entity whose private key is used to sign the
     * certificate.
     */
    public void setIssuerDN(
        X500Principal   issuer)
    {
        try
        {
            tbsGen.setIssuer(new X509Principal(issuer.getEncoded()));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }
    
    /**
     * Set the issuer distinguished name - the issuer is the entity whose private key is used to sign the
     * certificate.
     */
    public void setIssuerDN(
        X509Name   issuer)
    {
        tbsGen.setIssuer(issuer);
    }

    public void setNotBefore(
        Date    date)
    {
        tbsGen.setStartDate(new Time(date));
    }

    public void setNotAfter(
        Date    date)
    {
        tbsGen.setEndDate(new Time(date));
    }

    /**
     * Set the subject distinguished name. The subject describes the entity associated with the public key.
     */
    public void setSubjectDN(
        X500Principal   subject)
    {
        try
        {
            tbsGen.setSubject(new X509Principal(subject.getEncoded()));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("can't process principal: " + e);
        }
    }
    
    /**
     * Set the subject distinguished name. The subject describes the entity associated with the public key.
     */
    public void setSubjectDN(
        X509Name   subject)
    {
        tbsGen.setSubject(subject);
    }

    public void setPublicKey(
        PublicKey       key)
        throws IllegalArgumentException
    {
        try
        {
            tbsGen.setSubjectPublicKeyInfo(
                       SubjectPublicKeyInfo.getInstance(new ASN1InputStream(key.getEncoded()).readObject()));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("unable to process key - " + e.toString());
        }
    }

    /**
     * Set the signature algorithm. This can be either a name or an OID, names
     * are treated as case insensitive.
     * 
     * @param signatureAlgorithm string representation of the algorithm name.
     */
    public void setSignatureAlgorithm(
        String  signatureAlgorithm)
    {
        this.signatureAlgorithm = signatureAlgorithm;

        try
        {
            sigOID = X509Util.getAlgorithmOID(signatureAlgorithm);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unknown signature type requested: " + signatureAlgorithm);
        }

        sigAlgId = X509Util.getSigAlgID(sigOID, signatureAlgorithm);

        tbsGen.setSignature(sigAlgId);
    }

    /**
     * Set the subject unique ID - note: it is very rare that it is correct to do this.
     */
    public void setSubjectUniqueID(boolean[] uniqueID)
    {
        tbsGen.setSubjectUniqueID(booleanToBitString(uniqueID));
    }

    /**
     * Set the issuer unique ID - note: it is very rare that it is correct to do this.
     */
    public void setIssuerUniqueID(boolean[] uniqueID)
    {
        tbsGen.setIssuerUniqueID(booleanToBitString(uniqueID));
    }

    private DERBitString booleanToBitString(boolean[] id)
    {
        byte[] bytes = new byte[(id.length + 7) / 8];

        for (int i = 0; i != id.length; i++)
        {
            bytes[i / 8] |= (id[i]) ? (1 << ((7 - (i % 8)))) : 0;
        }

        int pad = id.length % 8;

        if (pad == 0)
        {
            return new DERBitString(bytes);
        }
        else
        {
            return new DERBitString(bytes, 8 - pad);
        }
    }
    
    /**
     * add a given extension field for the standard extensions tag (tag 3)
     */
    public void addExtension(
        String          oid,
        boolean         critical,
        DEREncodable    value)
    {
        this.addExtension(new DERObjectIdentifier(oid), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     */
    public void addExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        DEREncodable        value)
    {
        extGenerator.addExtension(oid, critical,  value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     * The value parameter becomes the contents of the octet string associated
     * with the extension.
     */
    public void addExtension(
        String          oid,
        boolean         critical,
        byte[]          value)
    {
        this.addExtension(new DERObjectIdentifier(oid), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     */
    public void addExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        byte[]              value)
    {
        extGenerator.addExtension(oid, critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     * copying the extension value from another certificate.
     * @throws CertificateParsingException if the extension cannot be extracted.
     */
    public void copyAndAddExtension(
        String          oid,
        boolean         critical,
        X509Certificate cert) 
        throws CertificateParsingException
    {
        byte[] extValue = cert.getExtensionValue(oid);
        
        if (extValue == null)
        {
            throw new CertificateParsingException("extension " + oid + " not present");
        }
        
        try
        {
            ASN1Encodable value = X509ExtensionUtil.fromExtensionValue(extValue);
    
            this.addExtension(oid, critical, value);
        }
        catch (IOException e)
        {
            throw new CertificateParsingException(e.toString());
        }
    }

    /**
     * add a given extension field for the standard extensions tag (tag 3)
     * copying the extension value from another certificate.
     * @throws CertificateParsingException if the extension cannot be extracted.
     */
    public void copyAndAddExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        X509Certificate     cert)
        throws CertificateParsingException
    {
        this.copyAndAddExtension(oid.getId(), critical, cert);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the default provider "BC".
     * @deprecated use generate(key, "BC")
     */
    public X509Certificate generateX509Certificate(
        PrivateKey      key)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509Certificate(key, "BC", null);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the default provider "BC", and the passed in source of randomness
     * (if required).
     * @deprecated use generate(key, random, "BC")
     */
    public X509Certificate generateX509Certificate(
        PrivateKey      key,
        SecureRandom    random)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509Certificate(key, "BC", random);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     * @deprecated use generate()
     */
    public X509Certificate generateX509Certificate(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateX509Certificate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     * @deprecated use generate()
     */
    public X509Certificate generateX509Certificate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generate(key, provider, random);
        }
        catch (NoSuchProviderException e)
        {
            throw e;
        }
        catch (SignatureException e)
        {
            throw e;
        }
        catch (InvalidKeyException e)
        {
            throw e;
        }
        catch (GeneralSecurityException e)
        {
            throw new SecurityException("exception: " + e);
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the default provider.
     * <p>
     * <b>Note:</b> this differs from the deprecated method in that the default provider is
     * used - not "BC".
     * </p>
     */
    public X509Certificate generate(
        PrivateKey      key)
        throws CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        return generate(key, (SecureRandom)null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the default provider, and the passed in source of randomness
     * (if required).
     * <p>
     * <b>Note:</b> this differs from the deprecated method in that the default provider is
     * used - not "BC".
     * </p>
     */
    public X509Certificate generate(
        PrivateKey      key,
        SecureRandom    random)
        throws CertificateEncodingException, IllegalStateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        TBSCertificateStructure tbsCert = generateTbsCert();
        byte[] signature;

        try
        {
            signature = X509Util.calculateSignature(sigOID, signatureAlgorithm, key, random, tbsCert);
        }
        catch (IOException e)
        {
            throw new ExtCertificateEncodingException("exception encoding TBS cert", e);
        }

        try
        {
            return generateJcaObject(tbsCert, signature);
        }
        catch (CertificateParsingException e)
        {
            throw new ExtCertificateEncodingException("exception producing certificate object", e);
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509Certificate generate(
        PrivateKey      key,
        String          provider)
        throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        return generate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     */
    public X509Certificate generate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        TBSCertificateStructure tbsCert = generateTbsCert();
        byte[] signature;

        try
        {
            signature = X509Util.calculateSignature(sigOID, signatureAlgorithm, provider, key, random, tbsCert);
        }
        catch (IOException e)
        {
            throw new ExtCertificateEncodingException("exception encoding TBS cert", e);
        }

        try
        {
            return generateJcaObject(tbsCert, signature);
        }
        catch (CertificateParsingException e)
        {
            throw new ExtCertificateEncodingException("exception producing certificate object", e);
        }
    }

    private TBSCertificateStructure generateTbsCert()
    {
        if (!extGenerator.isEmpty())
        {
            tbsGen.setExtensions(extGenerator.generate());
        }

        return tbsGen.generateTBSCertificate();
    }

    private X509Certificate generateJcaObject(TBSCertificateStructure tbsCert, byte[] signature)
        throws CertificateParsingException
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCert);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        return new X509CertificateObject(new X509CertificateStructure(new DERSequence(v)));
    }

    /**
     * Return an iterator of the signature names supported by the generator.
     * 
     * @return an iterator containing recognised names.
     */
    public Iterator getSignatureAlgNames()
    {
        return X509Util.getAlgNames();
    }
}
