package old.org.bouncycastle.cert.crmf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.crmf.EncryptedValue;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.KeyWrapper;
import old.org.bouncycastle.operator.OperatorException;
import old.org.bouncycastle.operator.OutputEncryptor;
import old.org.bouncycastle.util.Strings;

/**
 * Builder for EncryptedValue structures.
 */
public class EncryptedValueBuilder
{
    private KeyWrapper wrapper;
    private OutputEncryptor encryptor;
    private EncryptedValuePadder padder;

    /**
     * Create a builder that makes EncryptedValue structures.
     *
     * @param wrapper a wrapper for key used to encrypt the actual data contained in the EncryptedValue.
     * @param encryptor  an output encryptor to encrypt the actual data contained in the EncryptedValue. 
     */
    public EncryptedValueBuilder(KeyWrapper wrapper, OutputEncryptor encryptor)
    {
        this(wrapper, encryptor, null);
    }

    /**
     * Create a builder that makes EncryptedValue structures with fixed length blocks padded using the passed in padder.
     *
     * @param wrapper a wrapper for key used to encrypt the actual data contained in the EncryptedValue.
     * @param encryptor  an output encryptor to encrypt the actual data contained in the EncryptedValue.
     * @param padder a padder to ensure that the EncryptedValue created will always be a constant length.
     */
    public EncryptedValueBuilder(KeyWrapper wrapper, OutputEncryptor encryptor, EncryptedValuePadder padder)
    {
        this.wrapper = wrapper;
        this.encryptor = encryptor;
        this.padder = padder;
    }

    /**
     * Build an EncryptedValue structure containing the passed in pass phrase.
     *
     * @param revocationPassphrase  a revocation pass phrase.
     * @return an EncryptedValue containing the encrypted pass phrase.
     * @throws CRMFException on a failure to encrypt the data, or wrap the symmetric key for this value.
     */
    public EncryptedValue build(char[] revocationPassphrase)
        throws CRMFException
    {
        return encryptData(padData(Strings.toUTF8ByteArray(revocationPassphrase)));
    }

    /**
     * Build an EncryptedValue structure containing the certificate contained in
     * the passed in holder.
     *
     * @param holder  a holder containing a certificate.
     * @return an EncryptedValue containing the encrypted certificate.
     * @throws CRMFException on a failure to encrypt the data, or wrap the symmetric key for this value.
     */
    public EncryptedValue build(X509CertificateHolder holder)
        throws CRMFException
    {
        try
        {
            return encryptData(padData(holder.getEncoded()));
        }
        catch (IOException e)
        {
            throw new CRMFException("cannot encode certificate: " + e.getMessage(), e);
        }
    }

    private EncryptedValue encryptData(byte[] data)
       throws CRMFException
    {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        OutputStream eOut = encryptor.getOutputStream(bOut);

        try
        {
            eOut.write(data);

            eOut.close();
        }
        catch (IOException e)
        {
            throw new CRMFException("cannot process data: " + e.getMessage(), e);
        }

        AlgorithmIdentifier intendedAlg = null;
        AlgorithmIdentifier symmAlg = encryptor.getAlgorithmIdentifier();
        DERBitString encSymmKey;

        try
        {
            wrapper.generateWrappedKey(encryptor.getKey());
            encSymmKey = new DERBitString(wrapper.generateWrappedKey(encryptor.getKey()));
        }
        catch (OperatorException e)
        {
            throw new CRMFException("cannot wrap key: " + e.getMessage(), e);
        }

        AlgorithmIdentifier keyAlg = wrapper.getAlgorithmIdentifier();
        ASN1OctetString valueHint = null;
        DERBitString encValue = new DERBitString(bOut.toByteArray());

        return new EncryptedValue(intendedAlg, symmAlg, encSymmKey, keyAlg, valueHint, encValue);
    }

    private byte[] padData(byte[] data)
    {
        if (padder != null)
        {
            return padder.getPaddedData(data);
        }

        return data;
    }
}
