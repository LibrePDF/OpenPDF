package old.org.bouncycastle.tsp.cms;

import java.io.IOException;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.cms.Evidence;
import old.org.bouncycastle.asn1.cms.TimeStampAndCRL;
import old.org.bouncycastle.asn1.cms.TimeStampedData;
import old.org.bouncycastle.asn1.cms.TimeStampedDataParser;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.tsp.TSPException;
import old.org.bouncycastle.tsp.TimeStampToken;
import old.org.bouncycastle.tsp.TimeStampTokenInfo;
import old.org.bouncycastle.util.Arrays;

class TimeStampDataUtil
{
    private final TimeStampAndCRL[] timeStamps;

    private final MetaDataUtil      metaDataUtil;

    TimeStampDataUtil(TimeStampedData timeStampedData)
    {
        this.metaDataUtil = new MetaDataUtil(timeStampedData.getMetaData());

        Evidence evidence = timeStampedData.getTemporalEvidence();
        this.timeStamps = evidence.getTstEvidence().toTimeStampAndCRLArray();
    }

    TimeStampDataUtil(TimeStampedDataParser timeStampedData)
        throws IOException
    {       
        this.metaDataUtil = new MetaDataUtil(timeStampedData.getMetaData());

        Evidence evidence = timeStampedData.getTemporalEvidence();
        this.timeStamps = evidence.getTstEvidence().toTimeStampAndCRLArray();
    }

    TimeStampToken getTimeStampToken(TimeStampAndCRL timeStampAndCRL)
        throws CMSException
    {
        ContentInfo timeStampToken = timeStampAndCRL.getTimeStampToken();

        try
        {
            TimeStampToken token = new TimeStampToken(timeStampToken);
            return token;
        }
        catch (IOException e)
        {
            throw new CMSException("unable to parse token data: " + e.getMessage(), e);
        }
        catch (TSPException e)
        {
            if (e.getCause() instanceof CMSException)
            {
                throw (CMSException)e.getCause();
            }

            throw new CMSException("token data invalid: " + e.getMessage(), e);
        }
        catch (IllegalArgumentException e)
        {
            throw new CMSException("token data invalid: " + e.getMessage(), e);
        }
    }

    void initialiseMessageImprintDigestCalculator(DigestCalculator calculator)
        throws CMSException
    {
        metaDataUtil.initialiseMessageImprintDigestCalculator(calculator);
    }

    DigestCalculator getMessageImprintDigestCalculator(DigestCalculatorProvider calculatorProvider)
        throws OperatorCreationException
    {
        TimeStampToken token;

        try
        {
            token = this.getTimeStampToken(timeStamps[0]);

            TimeStampTokenInfo info = token.getTimeStampInfo();
            String algOID = info.getMessageImprintAlgOID();

            DigestCalculator calc = calculatorProvider.get(new AlgorithmIdentifier(algOID));

            initialiseMessageImprintDigestCalculator(calc);

            return calc;
        }
        catch (CMSException e)
        {
            throw new OperatorCreationException("unable to extract algorithm ID: " + e.getMessage(), e);
        }
    }

    TimeStampToken[] getTimeStampTokens()
        throws CMSException
    {
        TimeStampToken[] tokens = new TimeStampToken[timeStamps.length];
        for (int i = 0; i < timeStamps.length; i++)
        {
            tokens[i] = this.getTimeStampToken(timeStamps[i]);
        }

        return tokens;
    }

    TimeStampAndCRL[] getTimeStamps()
    {
        return timeStamps;
    }

    byte[] calculateNextHash(DigestCalculator calculator)
        throws CMSException
    {
        TimeStampAndCRL tspToken = timeStamps[timeStamps.length - 1];

        OutputStream out = calculator.getOutputStream();

        try
        {
            out.write(tspToken.getDEREncoded());

            out.close();

            return calculator.getDigest();
        }
        catch (IOException e)
        {
            throw new CMSException("exception calculating hash: " + e.getMessage(), e);
        }
    }

    /**
     * Validate the digests present in the TimeStampTokens contained in the CMSTimeStampedData.
     */
    void validate(DigestCalculatorProvider calculatorProvider, byte[] dataDigest)
        throws ImprintDigestInvalidException, CMSException
    {
        byte[] currentDigest = dataDigest;

        for (int i = 0; i < timeStamps.length; i++)
        {
            try
            {
                TimeStampToken token = this.getTimeStampToken(timeStamps[i]);
                if (i > 0)
                {
                    TimeStampTokenInfo info = token.getTimeStampInfo();
                    DigestCalculator calculator = calculatorProvider.get(info.getHashAlgorithm());

                    calculator.getOutputStream().write(timeStamps[i - 1].getDEREncoded());

                    currentDigest = calculator.getDigest();
                }

                this.compareDigest(token, currentDigest);
            }
            catch (IOException e)
            {
                throw new CMSException("exception calculating hash: " + e.getMessage(), e);
            }
            catch (OperatorCreationException e)
            {
                throw new CMSException("cannot create digest: " + e.getMessage(), e);
            }
        }
    }

    void validate(DigestCalculatorProvider calculatorProvider, byte[] dataDigest, TimeStampToken timeStampToken)
        throws ImprintDigestInvalidException, CMSException
    {
        byte[] currentDigest = dataDigest;
        byte[] encToken;

        try
        {
            encToken = timeStampToken.getEncoded();
        }
        catch (IOException e)
        {
            throw new CMSException("exception encoding timeStampToken: " + e.getMessage(), e);
        }

        for (int i = 0; i < timeStamps.length; i++)
        {
            try
            {
                TimeStampToken token = this.getTimeStampToken(timeStamps[i]);
                if (i > 0)
                {
                    TimeStampTokenInfo info = token.getTimeStampInfo();
                    DigestCalculator calculator = calculatorProvider.get(info.getHashAlgorithm());

                    calculator.getOutputStream().write(timeStamps[i - 1].getDEREncoded());

                    currentDigest = calculator.getDigest();
                }

                this.compareDigest(token, currentDigest);

                if (Arrays.areEqual(token.getEncoded(), encToken))
                {
                    return;
                }
            }
            catch (IOException e)
            {
                throw new CMSException("exception calculating hash: " + e.getMessage(), e);
            }
            catch (OperatorCreationException e)
            {
                throw new CMSException("cannot create digest: " + e.getMessage(), e);
            }
        }

        throw new ImprintDigestInvalidException("passed in token not associated with timestamps present", timeStampToken);
    }

    private void compareDigest(TimeStampToken timeStampToken, byte[] digest)
        throws ImprintDigestInvalidException
    {
        TimeStampTokenInfo info = timeStampToken.getTimeStampInfo();
        byte[] tsrMessageDigest = info.getMessageImprintDigest();

        if (!Arrays.areEqual(digest, tsrMessageDigest))
        {
            throw new ImprintDigestInvalidException("hash calculated is different from MessageImprintDigest found in TimeStampToken", timeStampToken);
        }
    }

    String getFileName()
    {
        return metaDataUtil.getFileName();
    }

    String getMediaType()
    {
        return metaDataUtil.getMediaType();
    }

    AttributeTable getOtherMetaData()
    {
        return new AttributeTable(metaDataUtil.getOtherMetaData());
    }
}
