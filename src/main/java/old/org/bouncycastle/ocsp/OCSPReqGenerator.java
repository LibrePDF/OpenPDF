package old.org.bouncycastle.ocsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1OutputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.ocsp.OCSPRequest;
import old.org.bouncycastle.asn1.ocsp.Request;
import old.org.bouncycastle.asn1.ocsp.Signature;
import old.org.bouncycastle.asn1.ocsp.TBSRequest;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.jce.X509Principal;

public class OCSPReqGenerator
{
    private List            list = new ArrayList();
    private GeneralName     requestorName = null;
    private X509Extensions  requestExtensions = null;

    private class RequestObject
    {
        CertificateID   certId;
        X509Extensions  extensions;

        public RequestObject(
            CertificateID   certId,
            X509Extensions  extensions)
        {
            this.certId = certId;
            this.extensions = extensions;
        }

        public Request toRequest()
            throws Exception
        {
            return new Request(certId.toASN1Object(), extensions);
        }
    }

    /**
     * Add a request for the given CertificateID.
     * 
     * @param certId certificate ID of interest
     */
    public void addRequest(
        CertificateID   certId)
    {
        list.add(new RequestObject(certId, null));
    }

    /**
     * Add a request with extensions
     * 
     * @param certId certificate ID of interest
     * @param singleRequestExtensions the extensions to attach to the request
     */
    public void addRequest(
        CertificateID   certId,
        X509Extensions  singleRequestExtensions)
    {
        list.add(new RequestObject(certId, singleRequestExtensions));
    }

    /**
     * Set the requestor name to the passed in X500Principal
     * 
     * @param requestorName a X500Principal representing the requestor name.
     */
    public void setRequestorName(
        X500Principal        requestorName)
    {
        try
        {
            this.requestorName = new GeneralName(GeneralName.directoryName, new X509Principal(requestorName.getEncoded()));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("cannot encode principal: " + e);
        }
    }

    public void setRequestorName(
        GeneralName         requestorName)
    {
        this.requestorName = requestorName;
    }
    
    public void setRequestExtensions(
        X509Extensions      requestExtensions)
    {
        this.requestExtensions = requestExtensions;
    }

    private OCSPReq generateRequest(
        DERObjectIdentifier signingAlgorithm,
        PrivateKey          key,
        X509Certificate[]   chain,
        String              provider,
        SecureRandom        random)
        throws OCSPException, NoSuchProviderException
    {
        Iterator    it = list.iterator();

        ASN1EncodableVector requests = new ASN1EncodableVector();

        while (it.hasNext())
        {
            try
            {
                requests.add(((RequestObject)it.next()).toRequest());
            }
            catch (Exception e)
            {
                throw new OCSPException("exception creating Request", e);
            }
        }

        TBSRequest  tbsReq = new TBSRequest(requestorName, new DERSequence(requests), requestExtensions);

        java.security.Signature sig = null;
        Signature               signature = null;

        if (signingAlgorithm != null)
        {
            if (requestorName == null)
            {
                throw new OCSPException("requestorName must be specified if request is signed.");
            }
            
            try
            {
                sig = OCSPUtil.createSignatureInstance(signingAlgorithm.getId(), provider);
                if (random != null)
                {
                    sig.initSign(key, random);
                }
                else
                {
                    sig.initSign(key);
                }
            }
            catch (NoSuchProviderException e)
            {
                // TODO Why this special case?
                throw e;
            }
            catch (GeneralSecurityException e)
            {
                throw new OCSPException("exception creating signature: " + e, e);
            }

            DERBitString    bitSig = null;

            try
            {
                ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
                ASN1OutputStream        aOut = new ASN1OutputStream(bOut);

                aOut.writeObject(tbsReq);

                sig.update(bOut.toByteArray());

                bitSig = new DERBitString(sig.sign());
            }
            catch (Exception e)
            {
                throw new OCSPException("exception processing TBSRequest: " + e, e);
            }

            AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(signingAlgorithm, new DERNull());

            if (chain != null && chain.length > 0)
            {
                ASN1EncodableVector v = new ASN1EncodableVector();
                try
                {
                    for (int i = 0; i != chain.length; i++)
                    {
                        v.add(new X509CertificateStructure(
                            (ASN1Sequence)ASN1Object.fromByteArray(chain[i].getEncoded())));
                    }
                }
                catch (IOException e)
                {
                    throw new OCSPException("error processing certs", e);
                }
                catch (CertificateEncodingException e)
                {
                    throw new OCSPException("error encoding certs", e);
                }

                signature = new Signature(sigAlgId, bitSig, new DERSequence(v));
            }
            else
            {
                signature = new Signature(sigAlgId, bitSig);
            }
        }

        return new OCSPReq(new OCSPRequest(tbsReq, signature));
    }
    
    /**
     * Generate an unsigned request
     * 
     * @return the OCSPReq
     * @throws OCSPException
     */
    public OCSPReq generate()
        throws OCSPException
    {
        try
        {
            return generateRequest(null, null, null, null, null);
        }
        catch (NoSuchProviderException e)
        {
            //
            // this shouldn't happen but...
            //
            throw new OCSPException("no provider! - " + e, e);
        }
    }

    public OCSPReq generate(
        String              signingAlgorithm,
        PrivateKey          key,
        X509Certificate[]   chain,
        String              provider)
        throws OCSPException, NoSuchProviderException, IllegalArgumentException
    {
        return generate(signingAlgorithm, key, chain, provider, null);
    }

    public OCSPReq generate(
        String              signingAlgorithm,
        PrivateKey          key,
        X509Certificate[]   chain,
        String              provider,
        SecureRandom        random)
        throws OCSPException, NoSuchProviderException, IllegalArgumentException
    {
        if (signingAlgorithm == null)
        {
            throw new IllegalArgumentException("no signing algorithm specified");
        }

        try
        {
            DERObjectIdentifier oid = OCSPUtil.getAlgorithmOID(signingAlgorithm);
            
            return generateRequest(oid, key, chain, provider, random);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("unknown signing algorithm specified: " + signingAlgorithm);
        }
    }
    
    /**
     * Return an iterator of the signature names supported by the generator.
     * 
     * @return an iterator containing recognised names.
     */
    public Iterator getSignatureAlgNames()
    {
        return OCSPUtil.getAlgNames();
    }
}
