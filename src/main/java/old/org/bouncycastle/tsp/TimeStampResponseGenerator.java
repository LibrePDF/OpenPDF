package old.org.bouncycastle.tsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERUTF8String;
import old.org.bouncycastle.asn1.cmp.PKIFailureInfo;
import old.org.bouncycastle.asn1.cmp.PKIFreeText;
import old.org.bouncycastle.asn1.cmp.PKIStatus;
import old.org.bouncycastle.asn1.cmp.PKIStatusInfo;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.tsp.TimeStampResp;

/**
 * Generator for RFC 3161 Time Stamp Responses.
 */
public class TimeStampResponseGenerator
{
    int status;

    ASN1EncodableVector statusStrings;

    int failInfo;
    private TimeStampTokenGenerator tokenGenerator;
    private Set                     acceptedAlgorithms;
    private Set                     acceptedPolicies;
    private Set                     acceptedExtensions;
    
    public TimeStampResponseGenerator(
        TimeStampTokenGenerator tokenGenerator,
        Set                     acceptedAlgorithms)
    {
        this(tokenGenerator, acceptedAlgorithms, null, null);
    }
    
    public TimeStampResponseGenerator(
        TimeStampTokenGenerator tokenGenerator,
        Set                     acceptedAlgorithms,
        Set                     acceptedPolicy)
    {
        this(tokenGenerator, acceptedAlgorithms, acceptedPolicy, null);
    }

    public TimeStampResponseGenerator(
        TimeStampTokenGenerator tokenGenerator,
        Set                     acceptedAlgorithms,
        Set                     acceptedPolicies,
        Set                     acceptedExtensions)
    {
        this.tokenGenerator = tokenGenerator;
        this.acceptedAlgorithms = acceptedAlgorithms;
        this.acceptedPolicies = acceptedPolicies;
        this.acceptedExtensions = acceptedExtensions;

        statusStrings = new ASN1EncodableVector();
    }

    private void addStatusString(String statusString)
    {
        statusStrings.add(new DERUTF8String(statusString));
    }

    private void setFailInfoField(int field)
    {
        failInfo = failInfo | field;
    }

    private PKIStatusInfo getPKIStatusInfo()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        
        v.add(new DERInteger(status));
        
        if (statusStrings.size() > 0)
        {
            v.add(new PKIFreeText(new DERSequence(statusStrings)));
        }

        if (failInfo != 0)
        {
            DERBitString failInfoBitString = new FailInfo(failInfo);
            v.add(failInfoBitString);
        }

        return new PKIStatusInfo(new DERSequence(v));
    }

    /**
     * Return an appropriate TimeStampResponse.
     * <p>
     * If genTime is null a timeNotAvailable error response will be returned.
     *
     * @param request the request this response is for.
     * @param serialNumber serial number for the response token.
     * @param genTime generation time for the response token.
     * @param provider provider to use for signature calculation.
     * @deprecated use method that does not require provider
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws TSPException
     */
    public TimeStampResponse generate(
        TimeStampRequest    request,
        BigInteger          serialNumber,
        Date                genTime,
        String              provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, TSPException
    {   
        TimeStampResp resp;
        
        try
        {
            if (genTime == null)
            {
                throw new TSPValidationException("The time source is not available.", PKIFailureInfo.timeNotAvailable);
            }

            request.validate(acceptedAlgorithms, acceptedPolicies, acceptedExtensions, provider);

            status = PKIStatus.GRANTED;
            this.addStatusString("Operation Okay");
            
            PKIStatusInfo pkiStatusInfo = getPKIStatusInfo();
            
            ContentInfo tstTokenContentInfo = null;
            try
            {
                ByteArrayInputStream    bIn = new ByteArrayInputStream(tokenGenerator.generate(request, serialNumber, genTime, provider).toCMSSignedData().getEncoded());
                ASN1InputStream         aIn = new ASN1InputStream(bIn);
                
                tstTokenContentInfo = ContentInfo.getInstance(aIn.readObject());
            }
            catch (java.io.IOException ioEx)
            {
                throw new TSPException(
                        "Timestamp token received cannot be converted to ContentInfo", ioEx);
            }
    
            resp = new TimeStampResp(pkiStatusInfo, tstTokenContentInfo);
        }
        catch (TSPValidationException e)
        {
            status = PKIStatus.REJECTION;
            
            this.setFailInfoField(e.getFailureCode());
            this.addStatusString(e.getMessage());
            
            PKIStatusInfo pkiStatusInfo = getPKIStatusInfo();

            resp = new TimeStampResp(pkiStatusInfo, null);
        }

        try
        {
            return new TimeStampResponse(resp);
        }
        catch (IOException e)
        {
            throw new TSPException("created badly formatted response!");
        }
    }

    /**
     * Return an appropriate TimeStampResponse.
     * <p>
     * If genTime is null a timeNotAvailable error response will be returned.
     *
     * @param request the request this response is for.
     * @param serialNumber serial number for the response token.
     * @param genTime generation time for the response token.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws TSPException
     */
    public TimeStampResponse generate(
        TimeStampRequest    request,
        BigInteger          serialNumber,
        Date                genTime)
        throws TSPException
    {
        TimeStampResp resp;

        try
        {
            if (genTime == null)
            {
                throw new TSPValidationException("The time source is not available.", PKIFailureInfo.timeNotAvailable);
            }

            request.validate(acceptedAlgorithms, acceptedPolicies, acceptedExtensions);

            status = PKIStatus.GRANTED;
            this.addStatusString("Operation Okay");

            PKIStatusInfo pkiStatusInfo = getPKIStatusInfo();

            ContentInfo tstTokenContentInfo = null;
            try
            {
                ByteArrayInputStream    bIn = new ByteArrayInputStream(tokenGenerator.generate(request, serialNumber, genTime).toCMSSignedData().getEncoded());
                ASN1InputStream         aIn = new ASN1InputStream(bIn);

                tstTokenContentInfo = ContentInfo.getInstance(aIn.readObject());
            }
            catch (java.io.IOException ioEx)
            {
                throw new TSPException(
                        "Timestamp token received cannot be converted to ContentInfo", ioEx);
            }

            resp = new TimeStampResp(pkiStatusInfo, tstTokenContentInfo);
        }
        catch (TSPValidationException e)
        {
            status = PKIStatus.REJECTION;

            this.setFailInfoField(e.getFailureCode());
            this.addStatusString(e.getMessage());

            PKIStatusInfo pkiStatusInfo = getPKIStatusInfo();

            resp = new TimeStampResp(pkiStatusInfo, null);
        }

        try
        {
            return new TimeStampResponse(resp);
        }
        catch (IOException e)
        {
            throw new TSPException("created badly formatted response!");
        }
    }

    class FailInfo extends DERBitString
    {
        FailInfo(int failInfoValue)
        {
            super(getBytes(failInfoValue), getPadBits(failInfoValue));
        }
    }

    /**
     * Generate a TimeStampResponse with chosen status and FailInfoField.
     * 
     * @param status the PKIStatus to set.
     * @param failInfoField the FailInfoField to set.
     * @param statusString an optional string describing the failure.
     * @return a TimeStampResponse with a failInfoField and optional statusString
     * @throws TSPException in case the response could not be created
     */
    public TimeStampResponse generateFailResponse(int status, int failInfoField, String statusString)
        throws TSPException
    {
        this.status = status;

        this.setFailInfoField(failInfoField);

        if (statusString != null)
        {
            this.addStatusString(statusString);
        }

        PKIStatusInfo pkiStatusInfo = getPKIStatusInfo();

        TimeStampResp resp = new TimeStampResp(pkiStatusInfo, null);

        try
        {
            return new TimeStampResponse(resp);
        }
        catch (IOException e)
        {
            throw new TSPException("created badly formatted response!");
        }
    }
}
