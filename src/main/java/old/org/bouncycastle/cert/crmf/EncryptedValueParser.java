package old.org.bouncycastle.cert.crmf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import old.org.bouncycastle.asn1.crmf.EncryptedValue;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.InputDecryptor;
import old.org.bouncycastle.util.Strings;
import old.org.bouncycastle.util.io.Streams;

/**
 * Parser for EncryptedValue structures.
 */
public class EncryptedValueParser
{
    private EncryptedValue value;
    private EncryptedValuePadder padder;

    /**
     * Basic constructor - create a parser to read the passed in value.
     *
     * @param value the value to be parsed.
     */
    public EncryptedValueParser(EncryptedValue value)
    {
        this.value = value;
    }

    /**
     * Create a parser to read the passed in value, assuming the padder was
     * applied to the data prior to encryption.
     *
     * @param value  the value to be parsed.
     * @param padder the padder to be used to remove padding from the decrypted value..
     */
    public EncryptedValueParser(EncryptedValue value, EncryptedValuePadder padder)
    {
        this.value = value;
        this.padder = padder;
    }

    private byte[] decryptValue(ValueDecryptorGenerator decGen)
        throws CRMFException
    {
        if (value.getIntendedAlg() != null)
        {
            throw new UnsupportedOperationException();
        }
        if (value.getValueHint() != null)
        {
            throw new UnsupportedOperationException();
        }

        InputDecryptor decryptor = decGen.getValueDecryptor(value.getKeyAlg(),
            value.getSymmAlg(), value.getEncSymmKey().getBytes());
        InputStream dataIn = decryptor.getInputStream(new ByteArrayInputStream(
            value.getEncValue().getBytes()));
        try
        {
            byte[] data = Streams.readAll(dataIn);

            if (padder != null)
            {
                return padder.getUnpaddedData(data);
            }
            
            return data;
        }
        catch (IOException e)
        {
            throw new CRMFException("Cannot parse decrypted data: " + e.getMessage(), e);
        }
    }

    /**
     * Read a X.509 certificate.
     *
     * @param decGen the decryptor generator to decrypt the encrypted value.
     * @return an X509CertificateHolder containing the certificate read.
     * @throws CRMFException if the decrypted data cannot be parsed, or a decryptor cannot be generated.
     */
    public X509CertificateHolder readCertificateHolder(ValueDecryptorGenerator decGen)
        throws CRMFException
    {
        return new X509CertificateHolder(X509CertificateStructure.getInstance(decryptValue(decGen)));
    }

    /**
     * Read a pass phrase.
     *
     * @param decGen the decryptor generator to decrypt the encrypted value.
     * @return a pass phrase as recovered from the encrypted value.
     * @throws CRMFException if the decrypted data cannot be parsed, or a decryptor cannot be generated.
     */
    public char[] readPassphrase(ValueDecryptorGenerator decGen)
        throws CRMFException
    {
        return Strings.fromUTF8ByteArray(decryptValue(decGen)).toCharArray();
    }
}
