package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.x509.CertificatePair;
import old.org.bouncycastle.x509.X509CertificatePair;
import old.org.bouncycastle.x509.X509StreamParserSpi;
import old.org.bouncycastle.x509.util.StreamParsingException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateParsingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class X509CertPairParser
    extends X509StreamParserSpi
{
    private InputStream currentStream = null;

    private X509CertificatePair readDERCrossCertificatePair(
        InputStream in)
        throws IOException, CertificateParsingException
    {
        ASN1InputStream dIn = new ASN1InputStream(in, ProviderUtil.getReadLimit(in));
        ASN1Sequence seq = (ASN1Sequence)dIn.readObject();
        CertificatePair pair = CertificatePair.getInstance(seq);
        return new X509CertificatePair(pair);
    }

    public void engineInit(InputStream in)
    {
        currentStream = in;

        if (!currentStream.markSupported())
        {
            currentStream = new BufferedInputStream(currentStream);
        }
    }

    public Object engineRead() throws StreamParsingException
    {
        try
        {

            currentStream.mark(10);
            int tag = currentStream.read();

            if (tag == -1)
            {
                return null;
            }

            currentStream.reset();
            return readDERCrossCertificatePair(currentStream);
        }
        catch (Exception e)
        {
            throw new StreamParsingException(e.toString(), e);
        }
    }

    public Collection engineReadAll() throws StreamParsingException
    {
        X509CertificatePair pair;
        List certs = new ArrayList();

        while ((pair = (X509CertificatePair)engineRead()) != null)
        {
            certs.add(pair);
        }

        return certs;
    }
}
