package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.SignedData;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * class for dealing with X509 certificates.
 * <p>
 * At the moment this will deal with "-----BEGIN CERTIFICATE-----" to "-----END CERTIFICATE-----"
 * base 64 encoded certs, as well as the BER binaries of certificates and some classes of PKCS#7
 * objects.
 */
public class JDKX509CertificateFactory
    extends CertificateFactorySpi
{
    private static final PEMUtil PEM_CERT_PARSER = new PEMUtil("CERTIFICATE");
    private static final PEMUtil PEM_CRL_PARSER = new PEMUtil("CRL");

    private ASN1Set            sData = null;
    private int                sDataObjectCount = 0;
    private InputStream        currentStream = null;
    
    private ASN1Set            sCrlData = null;
    private int                sCrlDataObjectCount = 0;
    private InputStream        currentCrlStream = null;

    private Certificate readDERCertificate(
        ASN1InputStream dIn)
        throws IOException, CertificateParsingException
    {
        ASN1Sequence    seq = (ASN1Sequence)dIn.readObject();

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

        return new X509CertificateObject(
                            X509CertificateStructure.getInstance(seq));
    }

    private Certificate getCertificate()
        throws CertificateParsingException
    {
        if (sData != null)
        {
            while (sDataObjectCount < sData.size())
            {
                Object obj = sData.getObjectAt(sDataObjectCount++);

                if (obj instanceof ASN1Sequence)
                {
                   return new X509CertificateObject(
                                    X509CertificateStructure.getInstance(obj));
                }
            }
        }

        return null;
    }

    private Certificate readPEMCertificate(
        InputStream  in)
        throws IOException, CertificateParsingException
    {
        ASN1Sequence seq = PEM_CERT_PARSER.readPEMObject(in);

        if (seq != null)
        {
            return new X509CertificateObject(
                            X509CertificateStructure.getInstance(seq));
        }

        return null;
    }

    protected CRL createCRL(CertificateList c)
    throws CRLException
    {
        return new X509CRLObject(c);
    }
    
    private CRL readPEMCRL(
        InputStream  in)
        throws IOException, CRLException
    {
        ASN1Sequence seq = PEM_CRL_PARSER.readPEMObject(in);

        if (seq != null)
        {
            return createCRL(
                            CertificateList.getInstance(seq));
        }

        return null;
    }

    private CRL readDERCRL(
        ASN1InputStream  aIn)
        throws IOException, CRLException
    {
        ASN1Sequence     seq = (ASN1Sequence)aIn.readObject();

        if (seq.size() > 1
                && seq.getObjectAt(0) instanceof DERObjectIdentifier)
        {
            if (seq.getObjectAt(0).equals(PKCSObjectIdentifiers.signedData))
            {
                sCrlData = new SignedData(ASN1Sequence.getInstance(
                                (ASN1TaggedObject)seq.getObjectAt(1), true)).getCRLs();
    
                return getCRL();
            }
        }

        return createCRL(
                     CertificateList.getInstance(seq));
    }

    private CRL getCRL()
        throws CRLException
    {
        if (sCrlData == null || sCrlDataObjectCount >= sCrlData.size())
        {
            return null;
        }

        return createCRL(
                            CertificateList.getInstance(
                                    sCrlData.getObjectAt(sCrlDataObjectCount++)));
    }

    /**
     * Generates a certificate object and initializes it with the data
     * read from the input stream inStream.
     */
    public Certificate engineGenerateCertificate(
        InputStream in) 
        throws CertificateException
    {
        if (currentStream == null)
        {
            currentStream = in;
            sData = null;
            sDataObjectCount = 0;
        }
        else if (currentStream != in) // reset if input stream has changed
        {
            currentStream = in;
            sData = null;
            sDataObjectCount = 0;
        }

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

            int limit = ProviderUtil.getReadLimit(in);

            PushbackInputStream pis = new PushbackInputStream(in);
            int tag = pis.read();

            if (tag == -1)
            {
                return null;
            }

            pis.unread(tag);

            if (tag != 0x30)  // assume ascii PEM encoded.
            {
                return readPEMCertificate(pis);
            }
            else
            {
                return readDERCertificate(new ASN1InputStream(pis, limit));
            }
        }
        catch (Exception e)
        {
            throw new CertificateException(e);
        }
    }

    /**
     * Returns a (possibly empty) collection view of the certificates
     * read from the given input stream inStream.
     */
    public Collection engineGenerateCertificates(
        InputStream inStream) 
        throws CertificateException
    {
        Certificate     cert;
        List            certs = new ArrayList();

        while ((cert = engineGenerateCertificate(inStream)) != null)
        {
            certs.add(cert);
        }

        return certs;
    }

    /**
     * Generates a certificate revocation list (CRL) object and initializes
     * it with the data read from the input stream inStream.
     */
    public CRL engineGenerateCRL(
        InputStream inStream) 
        throws CRLException
    {
        if (currentCrlStream == null)
        {
            currentCrlStream = inStream;
            sCrlData = null;
            sCrlDataObjectCount = 0;
        }
        else if (currentCrlStream != inStream) // reset if input stream has changed
        {
            currentCrlStream = inStream;
            sCrlData = null;
            sCrlDataObjectCount = 0;
        }

        try
        {
            if (sCrlData != null)
            {
                if (sCrlDataObjectCount != sCrlData.size())
                {
                    return getCRL();
                }
                else
                {
                    sCrlData = null;
                    sCrlDataObjectCount = 0;
                    return null;
                }
            }

            int limit = ProviderUtil.getReadLimit(inStream);

            PushbackInputStream pis = new PushbackInputStream(inStream);
            int tag = pis.read();

            if (tag == -1)
            {
                return null;
            }

            pis.unread(tag);

            if (tag != 0x30)  // assume ascii PEM encoded.
            {
                return readPEMCRL(pis);
            }
            else
            {       // lazy evaluate to help processing of large CRLs
                return readDERCRL(new ASN1InputStream(pis, limit, true));
            }
        }
        catch (CRLException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CRLException(e.toString());
        }
    }

    /**
     * Returns a (possibly empty) collection view of the CRLs read from
     * the given input stream inStream.
     *
     * The inStream may contain a sequence of DER-encoded CRLs, or
     * a PKCS#7 CRL set.  This is a PKCS#7 SignedData object, with the
     * only signficant field being crls.  In particular the signature
     * and the contents are ignored.
     */
    public Collection engineGenerateCRLs(
        InputStream inStream) 
        throws CRLException
    {
        CRL     crl;
        List    crls = new ArrayList();

        while ((crl = engineGenerateCRL(inStream)) != null)
        {
            crls.add(crl);
        }

        return crls;
    }

    public Iterator engineGetCertPathEncodings()
    {
        return PKIXCertPath.certPathEncodings.iterator();
    }

    public CertPath engineGenerateCertPath(
        InputStream inStream)
        throws CertificateException
    {
        return engineGenerateCertPath(inStream, "PkiPath");
    }

    public CertPath engineGenerateCertPath(
        InputStream inStream,
        String encoding)
        throws CertificateException
    {
        return new PKIXCertPath(inStream, encoding);
    }

    public CertPath engineGenerateCertPath(
        List certificates)
        throws CertificateException
    {
        Iterator iter = certificates.iterator();
        Object obj;
        while (iter.hasNext())
        {
            obj = iter.next();
            if (obj != null)
            {
                if (!(obj instanceof X509Certificate))
                {
                    throw new CertificateException("list contains non X509Certificate object while creating CertPath\n" + obj.toString());
                }
            }
        }
        return new PKIXCertPath(certificates);
    }
}
