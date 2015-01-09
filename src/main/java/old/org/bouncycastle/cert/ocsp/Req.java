package old.org.bouncycastle.cert.ocsp;

import old.org.bouncycastle.asn1.ocsp.Request;
import old.org.bouncycastle.asn1.x509.X509Extensions;

public class Req
{
    private Request req;

    public Req(
        Request req)
    {
        this.req = req;
    }

    public CertificateID getCertID()
    {
        return new CertificateID(req.getReqCert());
    }

    public X509Extensions getSingleRequestExtensions()
    {
        return req.getSingleRequestExtensions();
    }
}
