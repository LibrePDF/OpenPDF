package old.org.bouncycastle.crypto.tls;

import old.org.bouncycastle.asn1.x509.X509CertificateStructure;

/**
 * This should be implemented by any class which can find out, if a given certificate
 * chain is being accepted by an client.
 * 
 * @deprecated Perform certificate verification in TlsAuthentication implementation
 */
public interface CertificateVerifyer
{
    /**
     * @param certs The certs, which are part of the chain.
     * @return True, if the chain is accepted, false otherwise.
     */
    public boolean isValid(X509CertificateStructure[] certs);
}
