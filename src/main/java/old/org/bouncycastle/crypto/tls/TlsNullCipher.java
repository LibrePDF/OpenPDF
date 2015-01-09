package old.org.bouncycastle.crypto.tls;

/**
 * A NULL CipherSuite in java, this should only be used during handshake.
 */
public class TlsNullCipher implements TlsCipher
{
    public byte[] encodePlaintext(short type, byte[] plaintext, int offset, int len)
    {
        return copyData(plaintext, offset, len);
    }

    public byte[] decodeCiphertext(short type, byte[] ciphertext, int offset, int len)
    {
        return copyData(ciphertext, offset, len);
    }

    protected byte[] copyData(byte[] text, int offset, int len)
    {
        byte[] result = new byte[len];
        System.arraycopy(text, offset, result, 0, len);
        return result;
    }
}
