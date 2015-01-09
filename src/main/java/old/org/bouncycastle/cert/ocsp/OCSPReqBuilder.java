package old.org.bouncycastle.cert.ocsp;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.ocsp.OCSPRequest;
import old.org.bouncycastle.asn1.ocsp.Request;
import old.org.bouncycastle.asn1.ocsp.Signature;
import old.org.bouncycastle.asn1.ocsp.TBSRequest;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.ContentSigner;

public class OCSPReqBuilder
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
    public OCSPReqBuilder addRequest(
        CertificateID   certId)
    {
        list.add(new RequestObject(certId, null));

        return this;
    }

    /**
     * Add a request with extensions
     * 
     * @param certId certificate ID of interest
     * @param singleRequestExtensions the extensions to attach to the request
     */
    public OCSPReqBuilder addRequest(
        CertificateID   certId,
        X509Extensions  singleRequestExtensions)
    {
        list.add(new RequestObject(certId, singleRequestExtensions));

        return this;
    }

    /**
     * Set the requestor name to the passed in X500Principal
     * 
     * @param requestorName a X500Principal representing the requestor name.
     */
    public OCSPReqBuilder setRequestorName(
        X500Name requestorName)
    {
        this.requestorName = new GeneralName(GeneralName.directoryName, requestorName);

        return this;
    }

    public OCSPReqBuilder setRequestorName(
        GeneralName         requestorName)
    {
        this.requestorName = requestorName;

        return this;
    }
    
    public OCSPReqBuilder setRequestExtensions(
        X509Extensions      requestExtensions)
    {
        this.requestExtensions = requestExtensions;

        return this;
    }

    private OCSPReq generateRequest(
        ContentSigner           contentSigner,
        X509CertificateHolder[] chain)
        throws OCSPException
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

        Signature               signature = null;

        if (contentSigner != null)
        {
            if (requestorName == null)
            {
                throw new OCSPException("requestorName must be specified if request is signed.");
            }

            try
            {
                OutputStream sOut = contentSigner.getOutputStream();

                sOut.write(tbsReq.getDEREncoded());

                sOut.close();
            }
            catch (Exception e)
            {
                throw new OCSPException("exception processing TBSRequest: " + e, e);
            }

            DERBitString    bitSig = new DERBitString(contentSigner.getSignature());

            AlgorithmIdentifier sigAlgId = contentSigner.getAlgorithmIdentifier();

            if (chain != null && chain.length > 0)
            {
                ASN1EncodableVector v = new ASN1EncodableVector();

                for (int i = 0; i != chain.length; i++)
                {
                    v.add(chain[i].toASN1Structure());
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
     * @throws old.org.bouncycastle.ocsp.OCSPException
     */
    public OCSPReq build()
        throws OCSPException
    {
        return generateRequest(null, null);
    }

    public OCSPReq build(
        ContentSigner             signer,
        X509CertificateHolder[]   chain)
        throws OCSPException, IllegalArgumentException
    {
        if (signer == null)
        {
            throw new IllegalArgumentException("no signer specified");
        }

        return generateRequest(signer, chain);
    }
}
