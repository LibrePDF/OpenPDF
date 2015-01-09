package old.org.bouncycastle.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Extension;
import java.util.Date;

/**
 * Interface for an X.509 Attribute Certificate.
 */
public interface X509AttributeCertificate
    extends X509Extension
{   
    /**
     * Return the version number for the certificate.
     * 
     * @return the version number.
     */
    public int getVersion();
    
    /**
     * Return the serial number for the certificate.
     * 
     * @return the serial number.
     */
    public BigInteger getSerialNumber();
    
    /**
     * Return the date before which the certificate is not valid.
     * 
     * @return the "not valid before" date.
     */
    public Date getNotBefore();
    
    /**
     * Return the date after which the certificate is not valid.
     * 
     * @return the "not valid afer" date.
     */
    public Date getNotAfter();
    
    /**
     * Return the holder of the certificate.
     * 
     * @return the holder.
     */
    public AttributeCertificateHolder getHolder();
    
    /**
     * Return the issuer details for the certificate.
     * 
     * @return the issuer details.
     */
    public AttributeCertificateIssuer getIssuer();
    
    /**
     * Return the attributes contained in the attribute block in the certificate.
     * 
     * @return an array of attributes.
     */
    public X509Attribute[] getAttributes();
    
    /**
     * Return the attributes with the same type as the passed in oid.
     * 
     * @param oid the object identifier we wish to match.
     * @return an array of matched attributes, null if there is no match.
     */
    public X509Attribute[] getAttributes(String oid);
    
    public boolean[] getIssuerUniqueID();
    
    public void checkValidity()
        throws CertificateExpiredException, CertificateNotYetValidException;
    
    public void checkValidity(Date date)
        throws CertificateExpiredException, CertificateNotYetValidException;
    
    public byte[] getSignature();
    
    public void verify(PublicKey key, String provider)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException;
    
    /**
     * Return an ASN.1 encoded byte array representing the attribute certificate.
     * 
     * @return an ASN.1 encoded byte array.
     * @throws IOException if the certificate cannot be encoded.
     */
    public byte[] getEncoded()
        throws IOException;
}
