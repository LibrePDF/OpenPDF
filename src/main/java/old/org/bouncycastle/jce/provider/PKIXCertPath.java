package old.org.bouncycastle.jce.provider;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.pkcs.ContentInfo;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.SignedData;
import old.org.bouncycastle.openssl.PEMWriter;

/**
 * CertPath implementation for X.509 certificates.
 * <br />
 **/
public  class PKIXCertPath
    extends CertPath
{
    static final List certPathEncodings;

    static
    {
        List encodings = new ArrayList();
        encodings.add("PkiPath");
        encodings.add("PEM");
        encodings.add("PKCS7");
        certPathEncodings = Collections.unmodifiableList(encodings);
    }

    private List certificates;

    /**
     * @param certs
     */
    private List sortCerts(
        List certs)
    {
        if (certs.size() < 2)
        {
            return certs;
        }
        
        X500Principal   issuer = ((X509Certificate)certs.get(0)).getIssuerX500Principal();
        boolean         okay = true;
        
        for (int i = 1; i != certs.size(); i++) 
        {
            X509Certificate cert = (X509Certificate)certs.get(i);
            
            if (issuer.equals(cert.getSubjectX500Principal()))
            {
                issuer = ((X509Certificate)certs.get(i)).getIssuerX500Principal();
            }
            else
            {
                okay = false;
                break;
            }
        }
        
        if (okay)
        {
            return certs;
        }
        
        // find end-entity cert
        List       retList = new ArrayList(certs.size());
        List       orig = new ArrayList(certs);

        for (int i = 0; i < certs.size(); i++)
        {
            X509Certificate cert = (X509Certificate)certs.get(i);
            boolean         found = false;
            
            X500Principal   subject = cert.getSubjectX500Principal();
            
            for (int j = 0; j != certs.size(); j++)
            {
                X509Certificate c = (X509Certificate)certs.get(j);
                if (c.getIssuerX500Principal().equals(subject))
                {
                    found = true;
                    break;
                }
            }
            
            if (!found)
            {
                retList.add(cert);
                certs.remove(i);
            }
        }
        
        // can only have one end entity cert - something's wrong, give up.
        if (retList.size() > 1)
        {
            return orig;
        }

        for (int i = 0; i != retList.size(); i++)
        {
            issuer = ((X509Certificate)retList.get(i)).getIssuerX500Principal();
            
            for (int j = 0; j < certs.size(); j++)
            {
                X509Certificate c = (X509Certificate)certs.get(j);
                if (issuer.equals(c.getSubjectX500Principal()))
                {
                    retList.add(c);
                    certs.remove(j);
                    break;
                }
            }
        }
        
        // make sure all certificates are accounted for.
        if (certs.size() > 0)
        {
            return orig;
        }
        
        return retList;
    }

    PKIXCertPath(List certificates)
    {
        super("X.509");
        this.certificates = sortCerts(new ArrayList(certificates));
    }

    /**
     * Creates a CertPath of the specified type.
     * This constructor is protected because most users should use
     * a CertificateFactory to create CertPaths.
     **/
    PKIXCertPath(
        InputStream inStream,
        String encoding)
        throws CertificateException
    {
        super("X.509");
        try
        {
            if (encoding.equalsIgnoreCase("PkiPath"))
            {
                ASN1InputStream derInStream = new ASN1InputStream(inStream);
                DERObject derObject = derInStream.readObject();
                if (!(derObject instanceof ASN1Sequence))
                {
                    throw new CertificateException("input stream does not contain a ASN1 SEQUENCE while reading PkiPath encoded data to load CertPath");
                }
                Enumeration e = ((ASN1Sequence)derObject).getObjects();
                certificates = new ArrayList();
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
                while (e.hasMoreElements())
                {
                    ASN1Encodable element = (ASN1Encodable)e.nextElement();
                    byte[] encoded = element.getEncoded(ASN1Encodable.DER);
                    certificates.add(0, certFactory.generateCertificate(
                        new ByteArrayInputStream(encoded)));
                }
            }
            else if (encoding.equalsIgnoreCase("PKCS7") || encoding.equalsIgnoreCase("PEM"))
            {
                inStream = new BufferedInputStream(inStream);
                certificates = new ArrayList();
                CertificateFactory certFactory= CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
                Certificate cert;
                while ((cert = certFactory.generateCertificate(inStream)) != null)
                {
                    certificates.add(cert);
                }
            }
            else
            {
                throw new CertificateException("unsupported encoding: " + encoding);
            }
        }
        catch (IOException ex) 
        {
            throw new CertificateException("IOException throw while decoding CertPath:\n" + ex.toString()); 
        }
        catch (NoSuchProviderException ex) 
        {
            throw new CertificateException("BouncyCastle provider not found while trying to get a CertificateFactory:\n" + ex.toString()); 
        }
        
        this.certificates = sortCerts(certificates);
    }
    
    /**
     * Returns an iteration of the encodings supported by this
     * certification path, with the default encoding
     * first. Attempts to modify the returned Iterator via its
     * remove method result in an UnsupportedOperationException.
     *
     * @return an Iterator over the names of the supported encodings (as Strings)
     **/
    public Iterator getEncodings()
    {
        return certPathEncodings.iterator();
    }

    /**
     * Returns the encoded form of this certification path, using
     * the default encoding.
     *
     * @return the encoded bytes
     * @exception CertificateEncodingException if an encoding error occurs
     **/
    public byte[] getEncoded()
        throws CertificateEncodingException
    {
        Iterator iter = getEncodings();
        if (iter.hasNext())
        {
            Object enc = iter.next();
            if (enc instanceof String)
            {
            return getEncoded((String)enc);
            }
        }
        return null;
    }

    /**
     * Returns the encoded form of this certification path, using
     * the specified encoding.
     *
     * @param encoding the name of the encoding to use
     * @return the encoded bytes
     * @exception CertificateEncodingException if an encoding error
     * occurs or the encoding requested is not supported
     *
     **/
    public byte[] getEncoded(String encoding)
        throws CertificateEncodingException
    {
        if (encoding.equalsIgnoreCase("PkiPath"))
        {
            ASN1EncodableVector v = new ASN1EncodableVector();

            ListIterator iter = certificates.listIterator(certificates.size());
            while (iter.hasPrevious())
            {
                v.add(toASN1Object((X509Certificate)iter.previous()));
            }

            return toDEREncoded(new DERSequence(v));
        }
        else if (encoding.equalsIgnoreCase("PKCS7"))
        {
            ContentInfo encInfo = new ContentInfo(PKCSObjectIdentifiers.data, null);

            ASN1EncodableVector v = new ASN1EncodableVector();
            for (int i = 0; i != certificates.size(); i++)
            {
                v.add(toASN1Object((X509Certificate)certificates.get(i)));
            }
            
            SignedData  sd = new SignedData(
                                     new DERInteger(1),
                                     new DERSet(),
                                     encInfo, 
                                     new DERSet(v), 
                                     null, 
                                     new DERSet());

            return toDEREncoded(new ContentInfo(
                    PKCSObjectIdentifiers.signedData, sd));
        }
        else if (encoding.equalsIgnoreCase("PEM"))
        {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            PEMWriter             pWrt = new PEMWriter(new OutputStreamWriter(bOut));

            try
            {
                for (int i = 0; i != certificates.size(); i++)
                {
                    pWrt.writeObject(certificates.get(i));
                }
            
                pWrt.close();
            }
            catch (Exception e)
            {
                throw new CertificateEncodingException("can't encode certificate for PEM encoded path");
            }

            return bOut.toByteArray();
        }
        else
        {
            throw new CertificateEncodingException("unsupported encoding: " + encoding);
        }
    }

    /**
     * Returns the list of certificates in this certification
     * path. The List returned must be immutable and thread-safe. 
     *
     * @return an immutable List of Certificates (may be empty, but not null)
     **/
    public List getCertificates()
    {
        return Collections.unmodifiableList(new ArrayList(certificates));
    }

    /**
     * Return a DERObject containing the encoded certificate.
     *
     * @param cert the X509Certificate object to be encoded
     *
     * @return the DERObject
     **/
    private DERObject toASN1Object(
        X509Certificate cert)
        throws CertificateEncodingException
    {
        try
        {
            return new ASN1InputStream(cert.getEncoded()).readObject();
        }
        catch (Exception e)
        {
            throw new CertificateEncodingException("Exception while encoding certificate: " + e.toString());
        }
    }
    
    private byte[] toDEREncoded(ASN1Encodable obj) 
        throws CertificateEncodingException
    {
        try
        {
            return obj.getEncoded(ASN1Encodable.DER);
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException("Exception thrown: " + e);
        }
    }
}
