package old.org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsCipher
{
    byte[] encodePlaintext(short type, byte[] plaintext, int offset, int len) throws IOException;

    byte[] decodeCiphertext(short type, byte[] ciphertext, int offset, int len) throws IOException;
}
