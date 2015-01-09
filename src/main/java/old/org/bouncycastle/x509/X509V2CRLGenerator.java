package old.org.bouncycastle.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.TBSCertList;
import old.org.bouncycastle.asn1.x509.Time;
import old.org.bouncycastle.asn1.x509.V2TBSCertListGenerator;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
import old.org.bouncycastle.asn1.x509.X509Name;
import old.org.bouncycastle.jce.X509Principal;
import old.org.bouncycastle.jce.provider.X509CRLObject;

/**
 * class to produce an X.509 Version 2 CRL.
 *  @deprecated use org.bouncycastle.cert.X509v2CRLBuilder.
 */
public class X509V2CRLGenerator
{
    private V2TBSCertListGenerator      tbsGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private X509ExtensionsGenerator     extGenerator;

    public X509V2CRLGenerator()
    {
        tbsGen = new V2TBSCertListGenerator();
        extGenerator = new X509ExtensionsGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        tbsGen = new V2TBSCertListGenerator();
        extGenerator.reset();
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

    public void setThisUpdate(
        Date    date)
    {
        tbsGen.setThisUpdate(new Time(date));
    }

    public void setNextUpdate(
        Date    date)
    {
        tbsGen.setNextUpdate(new Time(date));
    }

    /**
     * Reason being as indicated by CRLReason, i.e. CRLReason.keyCompromise
     * or 0 if CRLReason is not to be used
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), reason);
    }

    /**
     * Add a CRL entry with an Invalidity Date extension as well as a CRLReason extension.
     * Reason being as indicated by CRLReason, i.e. CRLReason.keyCompromise
     * or 0 if CRLReason is not to be used
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, int reason, Date invalidityDate)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), reason, new DERGeneralizedTime(invalidityDate));
    }
   
    /**
     * Add a CRL entry with extensions.
     **/
    public void addCRLEntry(BigInteger userCertificate, Date revocationDate, X509Extensions extensions)
    {
        tbsGen.addCRLEntry(new DERInteger(userCertificate), new Time(revocationDate), extensions);
    }
    
    /**
     * Add the CRLEntry objects contained in a previous CRL.
     * 
     * @param other the X509CRL to source the other entries from. 
     */
    public void addCRL(X509CRL other)
        throws CRLException
    {
        Set revocations = other.getRevokedCertificates();

        if (revocations != null)
        {
            Iterator it = revocations.iterator();
            while (it.hasNext())
            {
                X509CRLEntry entry = (X509CRLEntry)it.next();

                ASN1InputStream aIn = new ASN1InputStream(entry.getEncoded());

                try
                {
                    tbsGen.addCRLEntry(ASN1Sequence.getInstance(aIn.readObject()));
                }
                catch (IOException e)
                {
                    throw new CRLException("exception processing encoding of CRL: " + e.toString());
                }
            }
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
            throw new IllegalArgumentException("Unknown signature type requested");
        }

        sigAlgId = X509Util.getSigAlgID(sigOID, signatureAlgorithm);

        tbsGen.setSignature(sigAlgId);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        String          oid,
        boolean         critical,
        DEREncodable    value)
    {
        this.addExtension(new DERObjectIdentifier(oid), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        DEREncodable        value)
    {
        extGenerator.addExtension(oid, critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        String          oid,
        boolean         critical,
        byte[]          value)
    {
        this.addExtension(new DERObjectIdentifier(oid), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag (tag 0)
     */
    public void addExtension(
        DERObjectIdentifier oid,
        boolean             critical,
        byte[]              value)
    {
        extGenerator.addExtension(oid, critical, value);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider "BC".
     * @deprecated use generate(key, "BC")
     */
    public X509CRL generateX509CRL(
        PrivateKey      key)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC", null);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider "BC" and an user defined SecureRandom object as
     * source of randomness.
     * @deprecated use generate(key, random, "BC")
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        SecureRandom    random)
        throws SecurityException, SignatureException, InvalidKeyException
    {
        try
        {
            return generateX509CRL(key, "BC", random);
        }
        catch (NoSuchProviderException e)
        {
            throw new SecurityException("BC provider not installed!");
        }
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the passed in provider for the signing.
     * @deprecated use generate()
     */
    public X509CRL generateX509CRL(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateX509CRL(key, provider, null);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject,
     * using the passed in provider for the signing.
     * @deprecated use generate()
     */
    public X509CRL generateX509CRL(
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
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider.
     * <p>
     * <b>Note:</b> this differs from the deprecated method in that the default provider is
     * used - not "BC".
     * </p>
     */
    public X509CRL generate(
        PrivateKey      key)
        throws CRLException, IllegalStateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        return generate(key, (SecureRandom)null);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject
     * using the default provider and an user defined SecureRandom object as
     * source of randomness.
     * <p>
     * <b>Note:</b> this differs from the deprecated method in that the default provider is
     * used - not "BC".
     * </p>
     */
    public X509CRL generate(
        PrivateKey      key,
        SecureRandom    random)
        throws CRLException, IllegalStateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        TBSCertList tbsCrl = generateCertList();
        byte[] signature;

        try
        {
            signature = X509Util.calculateSignature(sigOID, signatureAlgorithm, key, random, tbsCrl);
        }
        catch (IOException e)
        {
            throw new ExtCRLException("cannot generate CRL encoding", e);
        }

        return generateJcaObject(tbsCrl, signature);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject
     * using the passed in provider for the signing.
     */
    public X509CRL generate(
        PrivateKey      key,
        String          provider)
        throws CRLException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        return generate(key, provider, null);
    }

    /**
     * generate an X509 CRL, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509CRL generate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws CRLException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        TBSCertList tbsCrl = generateCertList();
        byte[] signature;

        try
        {
            signature = X509Util.calculateSignature(sigOID, signatureAlgorithm, provider, key, random, tbsCrl);
        }
        catch (IOException e)
        {
            throw new ExtCRLException("cannot generate CRL encoding", e);
        }

        return generateJcaObject(tbsCrl, signature);
    }

    private TBSCertList generateCertList()
    {
        if (!extGenerator.isEmpty())
        {
            tbsGen.setExtensions(extGenerator.generate());
        }

        return tbsGen.generateTBSCertList();
    }

    private X509CRL generateJcaObject(TBSCertList tbsCrl, byte[] signature)
        throws CRLException
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCrl);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        return new X509CRLObject(new CertificateList(new DERSequence(v)));
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

    private static class ExtCRLException
        extends CRLException
    {
        Throwable cause;

        ExtCRLException(String message, Throwable cause)
        {
            super(message);
            this.cause = cause;
        }

        public Throwable getCause()
        {
            return cause;
        }
    }
}
