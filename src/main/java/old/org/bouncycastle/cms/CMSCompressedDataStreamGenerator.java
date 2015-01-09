package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.BERSequenceGenerator;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequenceGenerator;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import old.org.bouncycastle.operator.OutputCompressor;

/**
 * General class for generating a compressed CMS message stream.
 * <p>
 * A simple example of usage.
 * </p>
 * <pre>
 *      CMSCompressedDataStreamGenerator gen = new CMSCompressedDataStreamGenerator();
 *      
 *      OutputStream cOut = gen.open(outputStream, new ZlibCompressor());
 *      
 *      cOut.write(data);
 *      
 *      cOut.close();
 * </pre>
 */
public class CMSCompressedDataStreamGenerator
{
    public static final String  ZLIB    = "1.2.840.113549.1.9.16.3.8";

    private int _bufferSize;
    
    /**
     * base constructor
     */
    public CMSCompressedDataStreamGenerator()
    {
    }

    /**
     * Set the underlying string size for encapsulated data
     *
     * @param bufferSize length of octet strings to buffer the data.
     */
    public void setBufferSize(
        int bufferSize)
    {
        _bufferSize = bufferSize;
    }

    /**
     * @deprecated use open(OutputStream, ContentCompressor)
     */
    public OutputStream open(
        OutputStream out,
        String       compressionOID) 
        throws IOException
    {
        return open(out, CMSObjectIdentifiers.data.getId(), compressionOID);
    }

    /**
     * @deprecated use open(OutputStream, ASN1ObjectIdentifier, ContentCompressor)
     */
    public OutputStream open(
        OutputStream  out,        
        String        contentOID,
        String        compressionOID) 
        throws IOException
    {
        BERSequenceGenerator sGen = new BERSequenceGenerator(out);
        
        sGen.addObject(CMSObjectIdentifiers.compressedData);
        
        //
        // Compressed Data
        //
        BERSequenceGenerator cGen = new BERSequenceGenerator(sGen.getRawOutputStream(), 0, true);
        
        cGen.addObject(new DERInteger(0));
        
        //
        // AlgorithmIdentifier
        //
        DERSequenceGenerator algGen = new DERSequenceGenerator(cGen.getRawOutputStream());
        
        algGen.addObject(new DERObjectIdentifier(ZLIB));

        algGen.close();
        
        //
        // Encapsulated ContentInfo
        //
        BERSequenceGenerator eiGen = new BERSequenceGenerator(cGen.getRawOutputStream());
        
        eiGen.addObject(new DERObjectIdentifier(contentOID));

        OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
            eiGen.getRawOutputStream(), 0, true, _bufferSize);
        
        return new CmsCompressedOutputStream(
            new DeflaterOutputStream(octetStream), sGen, cGen, eiGen);
    }

    public OutputStream open(
        OutputStream out,
        OutputCompressor compressor)
        throws IOException
    {
        return open(CMSObjectIdentifiers.data, out, compressor);
    }

    /**
     * Open a compressing output stream.
     *
     * @param contentOID
     * @param out
     * @param compressor
     * @return
     * @throws IOException
     */
    public OutputStream open(
        ASN1ObjectIdentifier contentOID,
        OutputStream out,
        OutputCompressor compressor)
        throws IOException
    {
        BERSequenceGenerator sGen = new BERSequenceGenerator(out);

        sGen.addObject(CMSObjectIdentifiers.compressedData);

        //
        // Compressed Data
        //
        BERSequenceGenerator cGen = new BERSequenceGenerator(sGen.getRawOutputStream(), 0, true);

        cGen.addObject(new DERInteger(0));

        //
        // AlgorithmIdentifier
        //
        cGen.addObject(compressor.getAlgorithmIdentifier());

        //
        // Encapsulated ContentInfo
        //
        BERSequenceGenerator eiGen = new BERSequenceGenerator(cGen.getRawOutputStream());

        eiGen.addObject(contentOID);

        OutputStream octetStream = CMSUtils.createBEROctetOutputStream(
            eiGen.getRawOutputStream(), 0, true, _bufferSize);

        return new CmsCompressedOutputStream(
            compressor.getOutputStream(octetStream), sGen, cGen, eiGen);
    }

    private class CmsCompressedOutputStream
        extends OutputStream
    {
        private OutputStream _out;
        private BERSequenceGenerator _sGen;
        private BERSequenceGenerator _cGen;
        private BERSequenceGenerator _eiGen;
        
        CmsCompressedOutputStream(
            OutputStream out,
            BERSequenceGenerator sGen,
            BERSequenceGenerator cGen,
            BERSequenceGenerator eiGen)
        {
            _out = out;
            _sGen = sGen;
            _cGen = cGen;
            _eiGen = eiGen;
        }
        
        public void write(
            int b)
            throws IOException
        {
            _out.write(b); 
        }
        
        
        public void write(
            byte[] bytes,
            int    off,
            int    len)
            throws IOException
        {
            _out.write(bytes, off, len);
        }
        
        public void write(
            byte[] bytes)
            throws IOException
        {
            _out.write(bytes);
        }
        
        public void close()
            throws IOException
        {
            _out.close();
            _eiGen.close();
            _cGen.close();
            _sGen.close();
        }
    }
}
