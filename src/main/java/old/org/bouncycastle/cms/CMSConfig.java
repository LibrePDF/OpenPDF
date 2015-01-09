package old.org.bouncycastle.cms;

import old.org.bouncycastle.asn1.DERObjectIdentifier;

public class CMSConfig
{
    /**
     * Set the mapping for the encryption algorithm used in association with a SignedData generation
     * or interpretation.
     *
     * @param oid object identifier to map.
     * @param algorithmName algorithm name to use.
     */
    public static void setSigningEncryptionAlgorithmMapping(String oid, String algorithmName)
    {
        DERObjectIdentifier id = new DERObjectIdentifier(oid);

        CMSSignedHelper.INSTANCE.setSigningEncryptionAlgorithmMapping(id, algorithmName);
    }

    /**
     * Set the mapping for the digest algorithm to use in conjunction with a SignedData generation
     * or interpretation.
     *
     * @param oid object identifier to map.
     * @param algorithmName algorithm name to use.
     */
    public static void setSigningDigestAlgorithmMapping(String oid, String algorithmName)
    {
        DERObjectIdentifier id = new DERObjectIdentifier(oid);

        CMSSignedHelper.INSTANCE.setSigningDigestAlgorithmMapping(id, algorithmName);
    }
}
