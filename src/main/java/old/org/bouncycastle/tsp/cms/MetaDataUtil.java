package old.org.bouncycastle.tsp.cms;

import java.io.IOException;

import old.org.bouncycastle.asn1.ASN1String;
import old.org.bouncycastle.asn1.cms.Attributes;
import old.org.bouncycastle.asn1.cms.MetaData;
import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.operator.DigestCalculator;

class MetaDataUtil
{
    private final MetaData          metaData;

    MetaDataUtil(MetaData metaData)
    {
        this.metaData = metaData;
    }

    void initialiseMessageImprintDigestCalculator(DigestCalculator calculator)
        throws CMSException
    {
        if (metaData != null && metaData.isHashProtected())
        {
            try
            {
                calculator.getOutputStream().write(metaData.getDEREncoded());
            }
            catch (IOException e)
            {
                throw new CMSException("unable to initialise calculator from metaData: " + e.getMessage(), e);
            }
        }
    }

    String getFileName()
    {
        if (metaData != null)
        {
            return convertString(metaData.getFileName());
        }

        return null;
    }

    String getMediaType()
    {
        if (metaData != null)
        {
            return convertString(metaData.getMediaType());
        }

        return null;
    }

    Attributes getOtherMetaData()
    {
        if (metaData != null)
        {
            return metaData.getOtherMetaData();
        }

        return null;
    }

    private String convertString(ASN1String s)
    {
        if (s != null)
        {
            return s.toString();
        }

        return null;
    }
}
