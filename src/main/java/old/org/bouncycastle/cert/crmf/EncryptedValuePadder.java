package old.org.bouncycastle.cert.crmf;

/**
 * An encrypted value padder is used to make sure that prior to a value been
 * encrypted the data is padded to a standard length.
 */
public interface EncryptedValuePadder
{
    /**
     * Return a byte array of padded data.
     *
     * @param data the data to be padded.
     * @return a padded byte array containing data.
     */
    byte[] getPaddedData(byte[] data);

    /**
     * Return a byte array of with padding removed.
     *
     * @param paddedData the data to be padded.
     * @return an array containing the original unpadded data.
     */
    byte[] getUnpaddedData(byte[] paddedData);
}
