package old.org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of the TLS 1.0 record layer.
 */
class RecordStream
{
    private TlsProtocolHandler handler;
    private InputStream is;
    private OutputStream os;
    private CombinedHash hash;
    private TlsCompression readCompression = null;
    private TlsCompression writeCompression = null;
    private TlsCipher readCipher = null;
    private TlsCipher writeCipher = null;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    RecordStream(TlsProtocolHandler handler, InputStream is, OutputStream os)
    {
        this.handler = handler;
        this.is = is;
        this.os = os;
        this.hash = new CombinedHash();
        this.readCompression = new TlsNullCompression();
        this.writeCompression = this.readCompression;
        this.readCipher = new TlsNullCipher();
        this.writeCipher = this.readCipher;
    }

    void clientCipherSpecDecided(TlsCompression tlsCompression, TlsCipher tlsCipher)
    {
        this.writeCompression = tlsCompression;
        this.writeCipher = tlsCipher;
    }

    void serverClientSpecReceived()
    {
        this.readCompression = this.writeCompression;
        this.readCipher = this.writeCipher;
    }

    public void readData() throws IOException
    {
        short type = TlsUtils.readUint8(is);
        TlsUtils.checkVersion(is, handler);
        int size = TlsUtils.readUint16(is);
        byte[] buf = decodeAndVerify(type, is, size);
        handler.processData(type, buf, 0, buf.length);
    }

    protected byte[] decodeAndVerify(short type, InputStream is, int len) throws IOException
    {
        byte[] buf = new byte[len];
        TlsUtils.readFully(buf, is);
        byte[] decoded = readCipher.decodeCiphertext(type, buf, 0, buf.length);

        OutputStream cOut = readCompression.decompress(buffer);

        if (cOut == buffer)
        {
            return decoded;
        }

        cOut.write(decoded, 0, decoded.length);
        cOut.flush();
        return getBufferContents();
    }

    protected void writeMessage(short type, byte[] message, int offset, int len) throws IOException
    {
        if (type == ContentType.handshake)
        {
            updateHandshakeData(message, offset, len);
        }

        OutputStream cOut = writeCompression.compress(buffer);

        byte[] ciphertext;
        if (cOut == buffer)
        {
            ciphertext = writeCipher.encodePlaintext(type, message, offset, len);
        }
        else
        {
            cOut.write(message, offset, len);
            cOut.flush();
            byte[] compressed = getBufferContents();
            ciphertext = writeCipher.encodePlaintext(type, compressed, 0, compressed.length);
        }

        byte[] writeMessage = new byte[ciphertext.length + 5];
        TlsUtils.writeUint8(type, writeMessage, 0);
        TlsUtils.writeVersion(writeMessage, 1);
        TlsUtils.writeUint16(ciphertext.length, writeMessage, 3);
        System.arraycopy(ciphertext, 0, writeMessage, 5, ciphertext.length);
        os.write(writeMessage);
        os.flush();
    }

    void updateHandshakeData(byte[] message, int offset, int len)
    {
        hash.update(message, offset, len);
    }

    byte[] getCurrentHash()
    {
        return doFinal(new CombinedHash(hash));
    }

    protected void close() throws IOException
    {
        IOException e = null;
        try
        {
            is.close();
        }
        catch (IOException ex)
        {
            e = ex;
        }
        try
        {
            os.close();
        }
        catch (IOException ex)
        {
            e = ex;
        }
        if (e != null)
        {
            throw e;
        }
    }

    protected void flush() throws IOException
    {
        os.flush();
    }

    private byte[] getBufferContents()
    {
        byte[] contents = buffer.toByteArray();
        buffer.reset();
        return contents;
    }

    private static byte[] doFinal(CombinedHash ch)
    {
        byte[] bs = new byte[ch.getDigestSize()];
        ch.doFinal(bs, 0);
        return bs;
    }
}
