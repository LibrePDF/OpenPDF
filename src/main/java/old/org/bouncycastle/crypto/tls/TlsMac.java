package old.org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.macs.HMac;
import old.org.bouncycastle.crypto.params.KeyParameter;

/**
 * A generic TLS MAC implementation, which can be used with any kind of Digest to act as
 * an HMAC.
 */
public class TlsMac
{
    protected long seqNo;
    protected HMac mac;

    /**
     * Generate a new instance of an TlsMac.
     * 
     * @param digest The digest to use.
     * @param key_block A byte-array where the key for this mac is located.
     * @param offset The number of bytes to skip, before the key starts in the buffer.
     * @param len The length of the key.
     */
    public TlsMac(Digest digest, byte[] key_block, int offset, int len)
    {
        this.mac = new HMac(digest);
        KeyParameter param = new KeyParameter(key_block, offset, len);
        this.mac.init(param);
        this.seqNo = 0;
    }

    /**
     * @return The Keysize of the mac.
     */
    public int getSize()
    {
        return mac.getMacSize();
    }

    /**
     * Calculate the mac for some given data.
     * <p/>
     * TlsMac will keep track of the sequence number internally.
     * 
     * @param type The message type of the message.
     * @param message A byte-buffer containing the message.
     * @param offset The number of bytes to skip, before the message starts.
     * @param len The length of the message.
     * @return A new byte-buffer containing the mac value.
     */
    public byte[] calculateMac(short type, byte[] message, int offset, int len)
    {
        ByteArrayOutputStream bosMac = new ByteArrayOutputStream(13);
        try
        {
            TlsUtils.writeUint64(seqNo++, bosMac);
            TlsUtils.writeUint8(type, bosMac);
            TlsUtils.writeVersion(bosMac);
            TlsUtils.writeUint16(len, bosMac);
        }
        catch (IOException e)
        {
            // This should never happen
            throw new IllegalStateException("Internal error during mac calculation");
        }

        byte[] macHeader = bosMac.toByteArray();
        mac.update(macHeader, 0, macHeader.length);
        mac.update(message, offset, len);

        byte[] result = new byte[mac.getMacSize()];
        mac.doFinal(result, 0);
        return result;
    }
}
