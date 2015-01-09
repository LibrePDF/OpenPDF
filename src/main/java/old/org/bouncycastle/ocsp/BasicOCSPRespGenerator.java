package old.org.bouncycastle.ocsp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import old.org.bouncycastle.asn1.ocsp.CertStatus;
import old.org.bouncycastle.asn1.ocsp.ResponseData;
import old.org.bouncycastle.asn1.ocsp.RevokedInfo;
import old.org.bouncycastle.asn1.ocsp.SingleResponse;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.CRLReason;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509Extensions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Generator for basic OCSP response objects.
 */
public class BasicOCSPRespGenerator
{
    private List            list = new ArrayList();
    private X509Extensions  responseExtensions = null;
    private RespID          responderID;

    private class ResponseObject
    {
        CertificateID         certId;
        CertStatus            certStatus;
        DERGeneralizedTime    thisUpdate;
        DERGeneralizedTime    nextUpdate;
        X509Extensions        extensions;

        public ResponseObject(
            CertificateID     certId,
            CertificateStatus certStatus,
            Date              thisUpdate,
            Date              nextUpdate,
            X509Extensions    extensions)
        {
            this.certId = certId;

            if (certStatus == null)
            {
                this.certStatus = new CertStatus();
            }
            else if (certStatus instanceof UnknownStatus)
            {
                this.certStatus = new CertStatus(2, new DERNull());
            }
            else 
            {
                RevokedStatus rs = (RevokedStatus)certStatus;
                
                if (rs.hasRevocationReason())
                {
                    this.certStatus = new CertStatus(
                                            new RevokedInfo(new DERGeneralizedTime(rs.getRevocationTime()), new CRLReason(rs.getRevocationReason())));
                }
                else
                {
                    this.certStatus = new CertStatus(
                                            new RevokedInfo(new DERGeneralizedTime(rs.getRevocationTime()), null));
                }
            }

            this.thisUpdate = new DERGeneralizedTime(thisUpdate);
            
            if (nextUpdate != null)
            {
                this.nextUpdate = new DERGeneralizedTime(nextUpdate);
            }
            else
            {
                this.nextUpdate = null;
            }

            this.extensions = extensions;
        }

        public SingleResponse toResponse()
            throws Exception
        {
            return new SingleResponse(certId.toASN1Object(), certStatus, thisUpdate, nextUpdate, extensions);
        }
    }

    /**
     * basic constructor
     */
    public BasicOCSPRespGenerator(
        RespID  responderID)
    {
        this.responderID = responderID;
    }

    /**
     * construct with the responderID to be the SHA-1 keyHash of the passed in public key.
     */
    public BasicOCSPRespGenerator(
        PublicKey       key)
        throws OCSPException
    {
        this.responderID = new RespID(key);
    }

    /**
     * Add a response for a particular Certificate ID.
     * 
     * @param certID certificate ID details
     * @param certStatus status of the certificate - null if okay
     */
    public void addResponse(
        CertificateID       certID,
        CertificateStatus   certStatus)
    {
        list.add(new ResponseObject(certID, certStatus, new Date(), null, null));
    }

    /**
     * Add a response for a particular Certificate ID.
     * 
     * @param certID certificate ID details
     * @param certStatus status of the certificate - null if okay
     * @param singleExtensions optional extensions
     */
    public void addResponse(
        CertificateID       certID,
        CertificateStatus   certStatus,
        X509Extensions      singleExtensions)
    {
        list.add(new ResponseObject(certID, certStatus, new Date(), null, singleExtensions));
    }
    
    /**
     * Add a response for a particular Certificate ID.
     * 
     * @param certID certificate ID details
     * @param nextUpdate date when next update should be requested
     * @param certStatus status of the certificate - null if okay
     * @param singleExtensions optional extensions
     */
    public void addResponse(
        CertificateID       certID,
        CertificateStatus   certStatus,
        Date                nextUpdate,
        X509Extensions      singleExtensions)
    {
        list.add(new ResponseObject(certID, certStatus, new Date(), nextUpdate, singleExtensions));
    }
    
    /**
     * Add a response for a particular Certificate ID.
     * 
     * @param certID certificate ID details
     * @param thisUpdate date this response was valid on
     * @param nextUpdate date when next update should be requested
     * @param certStatus status of the certificate - null if okay
     * @param singleExtensions optional extensions
     */
    public void addResponse(
        CertificateID       certID,
        CertificateStatus   certStatus,
        Date                thisUpdate,
        Date                nextUpdate,
        X509Extensions      singleExtensions)
    {
        list.add(new ResponseObject(certID, certStatus, thisUpdate, nextUpdate, singleExtensions));
    }
    
    /**
     * Set the extensions for the response.
     * 
     * @param responseExtensions the extension object to carry.
     */
    public void setResponseExtensions(
        X509Extensions  responseExtensions)
    {
        this.responseExtensions = responseExtensions;
    }

    private BasicOCSPResp generateResponse(
        String              signatureName,
        PrivateKey          key,
        X509Certificate[]   chain,
        Date                producedAt,
        String              provider,
        SecureRandom        random)
        throws OCSPException, NoSuchProviderException
    {
        Iterator    it = list.iterator();
        DERObjectIdentifier signingAlgorithm;

        try
        {
            signingAlgorithm = OCSPUtil.getAlgorithmOID(signatureName);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("unknown signing algorithm specified");
        }

        ASN1EncodableVector responses = new ASN1EncodableVector();

        while (it.hasNext())
        {
            try
            {
                responses.add(((ResponseObject)it.next()).toResponse());
            }
            catch (Exception e)
            {
                throw new OCSPException("exception creating Request", e);
            }
        }

        ResponseData  tbsResp = new ResponseData(responderID.toASN1Object(), new DERGeneralizedTime(producedAt), new DERSequence(responses), responseExtensions);

        Signature sig = null;

        try
        {
            sig = OCSPUtil.createSignatureInstance(signatureName, provider);
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
            sig.update(tbsResp.getEncoded(ASN1Encodable.DER));

            bitSig = new DERBitString(sig.sign());
        }
        catch (Exception e)
        {
            throw new OCSPException("exception processing TBSRequest: " + e, e);
        }

        AlgorithmIdentifier sigAlgId = OCSPUtil.getSigAlgID(signingAlgorithm);

        DERSequence chainSeq = null;
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

            chainSeq = new DERSequence(v);
        }

        return new BasicOCSPResp(new BasicOCSPResponse(tbsResp, sigAlgId, bitSig, chainSeq));
    }
    
    public BasicOCSPResp generate(
        String             signingAlgorithm,
        PrivateKey         key,
        X509Certificate[]  chain,
        Date               thisUpdate,
        String             provider)
        throws OCSPException, NoSuchProviderException, IllegalArgumentException
    {
        return generate(signingAlgorithm, key, chain, thisUpdate, provider, null);
    }

    public BasicOCSPResp generate(
        String             signingAlgorithm,
        PrivateKey         key,
        X509Certificate[]  chain,
        Date               producedAt,
        String             provider,
        SecureRandom       random)
        throws OCSPException, NoSuchProviderException, IllegalArgumentException
    {
        if (signingAlgorithm == null)
        {
            throw new IllegalArgumentException("no signing algorithm specified");
        }

        return generateResponse(signingAlgorithm, key, chain, producedAt, provider, random);
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
