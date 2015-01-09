package old.org.bouncycastle.tsp.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERTags;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.asn1.cms.ContentInfoParser;
import old.org.bouncycastle.asn1.cms.TimeStampedDataParser;
import old.org.bouncycastle.cms.CMSContentInfoParser;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.tsp.TimeStampToken;
import old.org.bouncycastle.util.io.Streams;

public class CMSTimeStampedDataParser
    extends CMSContentInfoParser
{
    private TimeStampedDataParser timeStampedData;
    private TimeStampDataUtil util;

    public CMSTimeStampedDataParser(InputStream in)
        throws CMSException
    {
        super(in);

        initialize(_contentInfo);
    }

    public CMSTimeStampedDataParser(byte[] baseData)
        throws CMSException
    {
        this(new ByteArrayInputStream(baseData));
    }

    private void initialize(ContentInfoParser contentInfo)
        throws CMSException
    {
        try
        {
            if (CMSObjectIdentifiers.timestampedData.equals(contentInfo.getContentType()))
            {
                this.timeStampedData = TimeStampedDataParser.getInstance(contentInfo.getContent(DERTags.SEQUENCE));
            }
            else
            {
                throw new IllegalArgumentException("Malformed content - type must be " + CMSObjectIdentifiers.timestampedData.getId());
            }
        }
        catch (IOException e)
        {
            throw new CMSException("parsing exception: " + e.getMessage(), e);
        }
    }

    public byte[] calculateNextHash(DigestCalculator calculator)
        throws CMSException
    {
        return util.calculateNextHash(calculator);
    }

    public InputStream getContent()
    {
        if (timeStampedData.getContent() != null)
        {
            return timeStampedData.getContent().getOctetStream();
        }

        return null;
    }

    public URI getDataUri()
        throws URISyntaxException
    {
        DERIA5String dataURI = this.timeStampedData.getDataUri();

        if (dataURI != null)
        {
           return new URI(dataURI.getString());
        }

        return null;
    }

    public String getFileName()
    {
        return util.getFileName();
    }

    public String getMediaType()
    {
        return util.getMediaType();
    }

    public AttributeTable getOtherMetaData()
    {
        return util.getOtherMetaData();
    }

    /**
     * Initialise the passed in calculator with the MetaData for this message, if it is
     * required as part of the initial message imprint calculation.
     *
     * @param calculator the digest calculator to be initialised.
     * @throws CMSException if the MetaData is required and cannot be processed
     */
    public void initialiseMessageImprintDigestCalculator(DigestCalculator calculator)
        throws CMSException
    {
        util.initialiseMessageImprintDigestCalculator(calculator);
    }

    /**
     * Returns an appropriately initialised digest calculator based on the message imprint algorithm
     * described in the first time stamp in the TemporalData for this message. If the metadata is required
     * to be included in the digest calculation, the returned calculator will be pre-initialised.
     *
     * @param calculatorProvider  a provider of DigestCalculator objects.
     * @return an initialised digest calculator.
     * @throws OperatorCreationException if the provider is unable to create the calculator.
     */
    public DigestCalculator getMessageImprintDigestCalculator(DigestCalculatorProvider calculatorProvider)
        throws OperatorCreationException
    {
        try
        {
            parseTimeStamps();
        }
        catch (CMSException e)
        {
            throw new OperatorCreationException("unable to extract algorithm ID: " + e.getMessage(), e);
        }

        return util.getMessageImprintDigestCalculator(calculatorProvider);
    }

    public TimeStampToken[] getTimeStampTokens()
        throws CMSException
    {
        parseTimeStamps();

        return util.getTimeStampTokens();
    }

    /**
     * Validate the digests present in the TimeStampTokens contained in the CMSTimeStampedData.
     *
     * @param calculatorProvider provider for digest calculators
     * @param dataDigest the calculated data digest for the message
     * @throws ImprintDigestInvalidException if an imprint digest fails to compare
     * @throws CMSException  if an exception occurs processing the message.
     */
    public void validate(DigestCalculatorProvider calculatorProvider, byte[] dataDigest)
        throws ImprintDigestInvalidException, CMSException
    {
        parseTimeStamps();

        util.validate(calculatorProvider, dataDigest);
    }

    /**
     * Validate the passed in timestamp token against the tokens and data present in the message.
     *
     * @param calculatorProvider provider for digest calculators
     * @param dataDigest the calculated data digest for the message.
     * @param timeStampToken  the timestamp token of interest.
     * @throws ImprintDigestInvalidException if the token is not present in the message, or an imprint digest fails to compare.
     * @throws CMSException if an exception occurs processing the message.
     */
    public void validate(DigestCalculatorProvider calculatorProvider, byte[] dataDigest, TimeStampToken timeStampToken)
        throws ImprintDigestInvalidException, CMSException
    {
        parseTimeStamps();

        util.validate(calculatorProvider, dataDigest, timeStampToken);
    }

    private void parseTimeStamps()
        throws CMSException
    {
        try
        {
            if (util == null)
            {
                InputStream cont = this.getContent();

                if (cont != null)
                {
                    Streams.drain(cont);
                }

                util = new TimeStampDataUtil(timeStampedData);
            }
        }
        catch (IOException e)
        {
            throw new CMSException("unable to parse evidence block: " + e.getMessage(), e);
        }
    }
}
