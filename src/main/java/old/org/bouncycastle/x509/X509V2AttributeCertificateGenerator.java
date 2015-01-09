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
import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.Iterator;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.AttCertIssuer;
import old.org.bouncycastle.asn1.x509.Attribute;
import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import old.org.bouncycastle.asn1.x509.V2AttributeCertificateInfoGenerator;
import old.org.bouncycastle.asn1.x509.X509ExtensionsGenerator;

/**
 * class to produce an X.509 Version 2 AttributeCertificate.
 * @deprecated use org.bouncycastle.cert.X509v2AttributeCertificateBuilder
 */
public class X509V2AttributeCertificateGenerator
{
    private V2AttributeCertificateInfoGenerator   acInfoGen;
    private DERObjectIdentifier         sigOID;
    private AlgorithmIdentifier         sigAlgId;
    private String                      signatureAlgorithm;
    private X509ExtensionsGenerator     extGenerator;

    public X509V2AttributeCertificateGenerator()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
        extGenerator = new X509ExtensionsGenerator();
    }

    /**
     * reset the generator
     */
    public void reset()
    {
        acInfoGen = new V2AttributeCertificateInfoGenerator();
        extGenerator.reset();
    }

    /**
     * Set the Holder of this Attribute Certificate
     */
    public void setHolder(
        AttributeCertificateHolder     holder)
    {
        acInfoGen.setHolder(holder.holder);
    }

    /**
     * Set the issuer
     */
    public void setIssuer(
        AttributeCertificateIssuer  issuer)
    {
        acInfoGen.setIssuer(AttCertIssuer.getInstance(issuer.form));
    }

    /**
     * set the serial number for the certificate.
     */
    public void setSerialNumber(
        BigInteger      serialNumber)
    {
        acInfoGen.setSerialNumber(new DERInteger(serialNumber));
    }

    public void setNotBefore(
        Date    date)
    {
        acInfoGen.setStartDate(new DERGeneralizedTime(date));
    }

    public void setNotAfter(
        Date    date)
    {
        acInfoGen.setEndDate(new DERGeneralizedTime(date));
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

        acInfoGen.setSignature(sigAlgId);
    }
    
    /**
     * add an attribute
     */
    public void addAttribute(
        X509Attribute       attribute)
    {
        acInfoGen.addAttribute(Attribute.getInstance(attribute.toASN1Object()));
    }

    public void setIssuerUniqueId(
        boolean[] iui)
    {
        // [TODO] convert boolean array to bit string
        //acInfoGen.setIssuerUniqueID(iui);
        throw new RuntimeException("not implemented (yet)");
    }
     
    /**
     * add a given extension field for the standard extensions tag
     * @throws IOException
     */
    public void addExtension(
        String          oid,
        boolean         critical,
        ASN1Encodable   value)
        throws IOException
    {
        extGenerator.addExtension(new DERObjectIdentifier(oid), critical, value);
    }

    /**
     * add a given extension field for the standard extensions tag
     * The value parameter becomes the contents of the octet string associated
     * with the extension.
     */
    public void addExtension(
        String          oid,
        boolean         critical,
        byte[]          value)
    {
        extGenerator.addExtension(new DERObjectIdentifier(oid), critical, value);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     * @deprecated use generate()
     */
    public X509AttributeCertificate generateCertificate(
        PrivateKey      key,
        String          provider)
        throws NoSuchProviderException, SecurityException, SignatureException, InvalidKeyException
    {
        return generateCertificate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     * @deprecated use generate()
     */
    public X509AttributeCertificate generateCertificate(
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
            throw new SecurityException("exception creating certificate: " + e);
        }
    }

   /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing.
     */
    public X509AttributeCertificate generate(
        PrivateKey      key,
        String          provider)
       throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, SignatureException, InvalidKeyException, NoSuchAlgorithmException
   {
        return generate(key, provider, null);
    }

    /**
     * generate an X509 certificate, based on the current issuer and subject,
     * using the passed in provider for the signing and the supplied source
     * of randomness, if required.
     */
    public X509AttributeCertificate generate(
        PrivateKey      key,
        String          provider,
        SecureRandom    random)
        throws CertificateEncodingException, IllegalStateException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException
    {
        if (!extGenerator.isEmpty())
        {
            acInfoGen.setExtensions(extGenerator.generate());
        }

        AttributeCertificateInfo acInfo = acInfoGen.generateAttributeCertificateInfo();

        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(acInfo);
        v.add(sigAlgId);

        try
        {
            v.add(new DERBitString(X509Util.calculateSignature(sigOID, signatureAlgorithm, provider, key, random, acInfo)));

            return new X509V2AttributeCertificate(new AttributeCertificate(new DERSequence(v)));
        }
        catch (IOException e)
        {
            throw new ExtCertificateEncodingException("constructed invalid certificate", e);
        }
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
