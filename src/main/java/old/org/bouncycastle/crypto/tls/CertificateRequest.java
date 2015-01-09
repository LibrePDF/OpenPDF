package old.org.bouncycastle.crypto.tls;

import java.util.Vector;

public class CertificateRequest
{
    private short[] certificateTypes;
    private Vector certificateAuthorities;

    public CertificateRequest(short[] certificateTypes, Vector certificateAuthorities)
    {
        this.certificateTypes = certificateTypes;
        this.certificateAuthorities = certificateAuthorities;
    }

    public short[] getCertificateTypes()
    {
        return certificateTypes;
    }

    /**
     * @return Vector of X500Name
     */
    public Vector getCertificateAuthorities()
    {
        return certificateAuthorities;
    }
}
