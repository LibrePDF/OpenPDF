package old.org.bouncycastle.tsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.cmp.PKIFailureInfo;
import old.org.bouncycastle.asn1.cmp.PKIFreeText;
import old.org.bouncycastle.asn1.cmp.PKIStatus;
import old.org.bouncycastle.asn1.cms.Attribute;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.tsp.TimeStampResp;
import old.org.bouncycastle.util.Arrays;

/**
 * Base class for an RFC 3161 Time Stamp Response object.
 */
public class TimeStampResponse
{
    TimeStampResp   resp;
    TimeStampToken  timeStampToken;

    public TimeStampResponse(TimeStampResp resp)
        throws TSPException, IOException
    {
        this.resp = resp;
        
        if (resp.getTimeStampToken() != null)
        {
            timeStampToken = new TimeStampToken(resp.getTimeStampToken());
        }
    }

    /**
     * Create a TimeStampResponse from a byte array containing an ASN.1 encoding.
     * 
     * @param resp the byte array containing the encoded response.
     * @throws TSPException if the response is malformed.
     * @throws IOException if the byte array doesn't represent an ASN.1 encoding.
     */
    public TimeStampResponse(byte[] resp)
        throws TSPException, IOException
    {
        this(new ByteArrayInputStream(resp));
    }

    /**
     * Create a TimeStampResponse from an input stream containing an ASN.1 encoding.
     * 
     * @param in the input stream containing the encoded response.
     * @throws TSPException if the response is malformed.
     * @throws IOException if the stream doesn't represent an ASN.1 encoding.
     */
    public TimeStampResponse(InputStream in)
        throws TSPException, IOException
    {
        this(readTimeStampResp(in));
    }

    private static TimeStampResp readTimeStampResp(
        InputStream in) 
        throws IOException, TSPException
    {
        try
        {
            return TimeStampResp.getInstance(new ASN1InputStream(in).readObject());
        }
        catch (IllegalArgumentException e)
        {
            throw new TSPException("malformed timestamp response: " + e, e);
        }
        catch (ClassCastException e)
        {
            throw new TSPException("malformed timestamp response: " + e, e);
        }
    }
    
    public int getStatus()
    {
        return resp.getStatus().getStatus().intValue();
    }

    public String getStatusString()
    {
        if (resp.getStatus().getStatusString() != null)
        {
            StringBuffer statusStringBuf = new StringBuffer();
            PKIFreeText text = resp.getStatus().getStatusString();
            for (int i = 0; i != text.size(); i++)
            {
                statusStringBuf.append(text.getStringAt(i).getString());
            }
            return statusStringBuf.toString();
        }
        else
        {
            return null;
        }
    }

    public PKIFailureInfo getFailInfo()
    {
        if (resp.getStatus().getFailInfo() != null)
        {
            return new PKIFailureInfo(resp.getStatus().getFailInfo());
        }
        
        return null;
    }

    public TimeStampToken getTimeStampToken()
    {
        return timeStampToken;
    }

    /**
     * Check this response against to see if it a well formed response for 
     * the passed in request. Validation will include checking the time stamp
     * token if the response status is GRANTED or GRANTED_WITH_MODS.
     * 
     * @param request the request to be checked against
     * @throws TSPException if the request can not match this response.
     */
    public void validate(
        TimeStampRequest    request)
        throws TSPException
    {
        TimeStampToken tok = this.getTimeStampToken();
        
        if (tok != null)
        {
            TimeStampTokenInfo  tstInfo = tok.getTimeStampInfo();
            
            if (request.getNonce() != null && !request.getNonce().equals(tstInfo.getNonce()))
            {
                throw new TSPValidationException("response contains wrong nonce value.");
            }
            
            if (this.getStatus() != PKIStatus.GRANTED && this.getStatus() != PKIStatus.GRANTED_WITH_MODS)
            {
                throw new TSPValidationException("time stamp token found in failed request.");
            }
            
            if (!Arrays.constantTimeAreEqual(request.getMessageImprintDigest(), tstInfo.getMessageImprintDigest()))
            {
                throw new TSPValidationException("response for different message imprint digest.");
            }
            
            if (!tstInfo.getMessageImprintAlgOID().equals(request.getMessageImprintAlgOID()))
            {
                throw new TSPValidationException("response for different message imprint algorithm.");
            }

            Attribute scV1 = tok.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificate);
            Attribute scV2 = tok.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificateV2);

            if (scV1 == null && scV2 == null)
            {
                throw new TSPValidationException("no signing certificate attribute present.");
            }

            if (scV1 != null && scV2 != null)
            {
                throw new TSPValidationException("conflicting signing certificate attributes present.");
            }

            if (request.getReqPolicy() != null && !request.getReqPolicy().equals(tstInfo.getPolicy()))
            {
                throw new TSPValidationException("TSA policy wrong for request.");
            }
        }
        else if (this.getStatus() == PKIStatus.GRANTED || this.getStatus() == PKIStatus.GRANTED_WITH_MODS)
        {
            throw new TSPValidationException("no time stamp token found and one expected.");
        }
    }
    
    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded() throws IOException
    {
        return resp.getEncoded();
    }
}