package old.org.bouncycastle.x509.extension;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.GeneralNames;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.PrincipalUtil;

/**
 * A high level authority key identifier.
 */
public class AuthorityKeyIdentifierStructure
    extends AuthorityKeyIdentifier
{
    /**
     * Constructor which will take the byte[] returned from getExtensionValue()
     * 
     * @param encodedValue a DER octet encoded string with the extension structure in it.
     * @throws IOException on parsing errors.
     */
    public AuthorityKeyIdentifierStructure(
        byte[]  encodedValue)
        throws IOException
    {
        super((ASN1Sequence)X509ExtensionUtil.fromExtensionValue(encodedValue));
    }

    /**
     * Constructor which will take an extension
     *
     * @param extension a X509Extension object containing an AuthorityKeyIdentifier.
     */
    public AuthorityKeyIdentifierStructure(
        X509Extension extension)
    {
        super((ASN1Sequence)extension.getParsedValue());
    }

    private static ASN1Sequence fromCertificate(
        X509Certificate certificate)
        throws CertificateParsingException
    {
        try
        {
            if (certificate.getVersion() != 3)
            {
                GeneralName          genName = new GeneralName(PrincipalUtil.getIssuerX509Principal(certificate));
                SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                        (ASN1Sequence)new ASN1InputStream(certificate.getPublicKey().getEncoded()).readObject());
                
                return (ASN1Sequence)new AuthorityKeyIdentifier(
                               info, new GeneralNames(genName), certificate.getSerialNumber()).toASN1Object();
            }
            else
            {
                GeneralName             genName = new GeneralName(PrincipalUtil.getIssuerX509Principal(certificate));
                
                byte[]                  ext = certificate.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
                
                if (ext != null)
                {
                    ASN1OctetString     str = (ASN1OctetString)X509ExtensionUtil.fromExtensionValue(ext);
                
                    return (ASN1Sequence)new AuthorityKeyIdentifier(
                                    str.getOctets(), new GeneralNames(genName), certificate.getSerialNumber()).toASN1Object();
                }
                else
                {
                    SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                            (ASN1Sequence)new ASN1InputStream(certificate.getPublicKey().getEncoded()).readObject());
                    
                    return (ASN1Sequence)new AuthorityKeyIdentifier(
                            info, new GeneralNames(genName), certificate.getSerialNumber()).toASN1Object();
                }
            }
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("Exception extracting certificate details: " + e.toString());
        }
    }
    
    private static ASN1Sequence fromKey(
        PublicKey pubKey)
        throws InvalidKeyException
    {
        try
        {
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                                        (ASN1Sequence)new ASN1InputStream(pubKey.getEncoded()).readObject());
        
            return (ASN1Sequence)new AuthorityKeyIdentifier(info).toASN1Object();
        }
        catch (Exception e)
        {
            throw new InvalidKeyException("can't process key: " + e);
        }
    }
    
    /**
     * Create an AuthorityKeyIdentifier using the passed in certificate's public
     * key, issuer and serial number.
     * 
     * @param certificate the certificate providing the information.
     * @throws CertificateParsingException if there is a problem processing the certificate
     */
    public AuthorityKeyIdentifierStructure(
        X509Certificate certificate)
        throws CertificateParsingException
    {
        super(fromCertificate(certificate));
    }
    
    /**
     * Create an AuthorityKeyIdentifier using just the hash of the 
     * public key.
     * 
     * @param pubKey the key to generate the hash from.
     * @throws InvalidKeyException if there is a problem using the key.
     */
    public AuthorityKeyIdentifierStructure(
        PublicKey pubKey) 
        throws InvalidKeyException
    {
        super(fromKey(pubKey));
    }
}
