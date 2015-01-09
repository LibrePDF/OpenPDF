package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.SignedData;
import old.org.bouncycastle.x509.X509AttributeCertificate;
import old.org.bouncycastle.x509.X509StreamParserSpi;
import old.org.bouncycastle.x509.X509V2AttributeCertificate;
import old.org.bouncycastle.x509.util.StreamParsingException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class X509AttrCertParser
    extends X509StreamParserSpi
{
    private static final PEMUtil PEM_PARSER = new PEMUtil("ATTRIBUTE CERTIFICATE");

    private ASN1Set     sData = null;
    private int         sDataObjectCount = 0;
    private InputStream currentStream = null;

    private X509AttributeCertificate readDERCertificate(
        InputStream in)
        throws IOException
    {
        ASN1InputStream dIn = new ASN1InputStream(in, ProviderUtil.getReadLimit(in));
        ASN1Sequence seq = (ASN1Sequence)dIn.readObject();

        if (seq.size() > 1
                && seq.getObjectAt(0) instanceof DERObjectIdentifier)
        {
            if (seq.getObjectAt(0).equals(PKCSObjectIdentifiers.signedData))
            {
                sData = new SignedData(ASN1Sequence.getInstance(
                                (ASN1TaggedObject)seq.getObjectAt(1), true)).getCertificates();

                return getCertificate();
            }
        }

        return new X509V2AttributeCertificate(seq.getEncoded());
    }

    private X509AttributeCertificate getCertificate()
        throws IOException
    {
        if (sData != null)
        {
            while (sDataObjectCount < sData.size())
            {
                Object obj = sData.getObjectAt(sDataObjectCount++);

                if (obj instanceof ASN1TaggedObject && ((ASN1TaggedObject)obj).getTagNo() == 2)
                {
                   return new X509V2AttributeCertificate(
                          ASN1Sequence.getInstance((ASN1TaggedObject)obj, false).getEncoded());
                }
            }
        }

        return null;
    }

    private X509AttributeCertificate readPEMCertificate(
        InputStream  in)
        throws IOException
    {
        ASN1Sequence seq = PEM_PARSER.readPEMObject(in);

        if (seq != null)
        {
            return new X509V2AttributeCertificate(seq.getEncoded());
        }

        return null;
    }

    public void engineInit(InputStream in)
    {
        currentStream = in;
        sData = null;
        sDataObjectCount = 0;

        if (!currentStream.markSupported())
        {
            currentStream = new BufferedInputStream(currentStream);
        }
    }

    public Object engineRead()
        throws StreamParsingException
    {
        try
        {
            if (sData != null)
            {
                if (sDataObjectCount != sData.size())
                {
                    return getCertificate();
                }
                else
                {
                    sData = null;
                    sDataObjectCount = 0;
                    return null;
                }
            }

            currentStream.mark(10);
            int    tag = currentStream.read();

            if (tag == -1)
            {
                return null;
            }

            if (tag != 0x30)  // assume ascii PEM encoded.
            {
                currentStream.reset();
                return readPEMCertificate(currentStream);
            }
            else
            {
                currentStream.reset();
                return readDERCertificate(currentStream);
            }
        }
        catch (Exception e)
        {
            throw new StreamParsingException(e.toString(), e);
        }
    }

    public Collection engineReadAll()
        throws StreamParsingException
    {
        X509AttributeCertificate cert;
        List certs = new ArrayList();

        while ((cert = (X509AttributeCertificate)engineRead()) != null)
        {
            certs.add(cert);
        }

        return certs;
    }
}
