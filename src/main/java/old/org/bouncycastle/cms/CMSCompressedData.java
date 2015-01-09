package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.cms.CompressedData;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.operator.InputExpander;
import old.org.bouncycastle.operator.InputExpanderProvider;

/**
 * containing class for an CMS Compressed Data object
 * <pre>
 *     CMSCompressedData cd = new CMSCompressedData(inputStream);
 *
 *     process(cd.getContent(new ZlibExpanderProvider()));
 * </pre>
 */
public class CMSCompressedData
{
    ContentInfo                 contentInfo;

    public CMSCompressedData(
        byte[]    compressedData) 
        throws CMSException
    {
        this(CMSUtils.readContentInfo(compressedData));
    }

    public CMSCompressedData(
        InputStream    compressedData) 
        throws CMSException
    {
        this(CMSUtils.readContentInfo(compressedData));
    }

    public CMSCompressedData(
        ContentInfo contentInfo)
        throws CMSException
    {
        this.contentInfo = contentInfo;
    }

    /**
     * Return the uncompressed content.
     *
     * @return the uncompressed content
     * @throws CMSException if there is an exception uncompressing the data.
     * @deprecated use getContent(InputExpanderProvider)
     */
    public byte[] getContent()
        throws CMSException
    {
        CompressedData  comData = CompressedData.getInstance(contentInfo.getContent());
        ContentInfo     content = comData.getEncapContentInfo();

        ASN1OctetString bytes = (ASN1OctetString)content.getContent();

        InflaterInputStream     zIn = new InflaterInputStream(bytes.getOctetStream());

        try
        {
            return CMSUtils.streamToByteArray(zIn);
        }
        catch (IOException e)
        {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    /**
     * Return the uncompressed content, throwing an exception if the data size
     * is greater than the passed in limit. If the content is exceeded getCause()
     * on the CMSException will contain a StreamOverflowException
     *
     * @param limit maximum number of bytes to read
     * @return the content read
     * @throws CMSException if there is an exception uncompressing the data.
     * @deprecated use getContent(InputExpanderProvider)
     */
    public byte[] getContent(int limit)
        throws CMSException
    {
        CompressedData  comData = CompressedData.getInstance(contentInfo.getContent());
        ContentInfo     content = comData.getEncapContentInfo();

        ASN1OctetString bytes = (ASN1OctetString)content.getContent();

        InflaterInputStream     zIn = new InflaterInputStream(bytes.getOctetStream());

        try
        {
            return CMSUtils.streamToByteArray(zIn, limit);
        }
        catch (IOException e)
        {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    public ASN1ObjectIdentifier getContentType()
    {
        return contentInfo.getContentType();
    }

    /**
     * Return the uncompressed content.
     *
     * @param expanderProvider a provider of expander algorithm implementations.
     * @return the uncompressed content
     * @throws CMSException if there is an exception un-compressing the data.
     */
    public byte[] getContent(InputExpanderProvider expanderProvider)
        throws CMSException
    {
        CompressedData  comData = CompressedData.getInstance(contentInfo.getContent());
        ContentInfo     content = comData.getEncapContentInfo();

        ASN1OctetString bytes = (ASN1OctetString)content.getContent();
        InputExpander   expander = expanderProvider.get(comData.getCompressionAlgorithmIdentifier());
        InputStream     zIn = expander.getInputStream(bytes.getOctetStream());

        try
        {
            return CMSUtils.streamToByteArray(zIn);
        }
        catch (IOException e)
        {
            throw new CMSException("exception reading compressed stream.", e);
        }
    }

    /**
     * return the ContentInfo 
     */
    public ContentInfo getContentInfo()
    {
        return contentInfo;
    }
    
    /**
     * return the ASN.1 encoded representation of this object.
     */
    public byte[] getEncoded()
        throws IOException
    {
        return contentInfo.getEncoded();
    }
}
