package old.org.bouncycastle.tsp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.cms.Attribute;
import old.org.bouncycastle.asn1.cms.AttributeTable;
import old.org.bouncycastle.asn1.cms.ContentInfo;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import old.org.bouncycastle.asn1.x509.KeyPurposeId;
import old.org.bouncycastle.asn1.x509.X509Extension;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.cms.SignerInformation;
import old.org.bouncycastle.operator.DigestCalculator;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;
import old.org.bouncycastle.util.Arrays;

public class TSPUtil
{
    private static Set EMPTY_SET = Collections.unmodifiableSet(new HashSet());
    private static List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());

    private static final Map digestLengths = new HashMap();
    private static final Map digestNames = new HashMap();

    static
    {
        digestLengths.put(PKCSObjectIdentifiers.md5.getId(), new Integer(16));
        digestLengths.put(OIWObjectIdentifiers.idSHA1.getId(), new Integer(20));
        digestLengths.put(NISTObjectIdentifiers.id_sha224.getId(), new Integer(28));
        digestLengths.put(NISTObjectIdentifiers.id_sha256.getId(), new Integer(32));
        digestLengths.put(NISTObjectIdentifiers.id_sha384.getId(), new Integer(48));
        digestLengths.put(NISTObjectIdentifiers.id_sha512.getId(), new Integer(64));
        digestLengths.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), new Integer(16));
        digestLengths.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), new Integer(20));
        digestLengths.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), new Integer(32));
        digestLengths.put(CryptoProObjectIdentifiers.gostR3411.getId(), new Integer(32));

        digestNames.put(PKCSObjectIdentifiers.md5.getId(), "MD5");
        digestNames.put(OIWObjectIdentifiers.idSHA1.getId(), "SHA1");
        digestNames.put(NISTObjectIdentifiers.id_sha224.getId(), "SHA224");
        digestNames.put(NISTObjectIdentifiers.id_sha256.getId(), "SHA256");
        digestNames.put(NISTObjectIdentifiers.id_sha384.getId(), "SHA384");
        digestNames.put(NISTObjectIdentifiers.id_sha512.getId(), "SHA512");
        digestNames.put(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId(), "SHA1");
        digestNames.put(PKCSObjectIdentifiers.sha224WithRSAEncryption.getId(), "SHA224");
        digestNames.put(PKCSObjectIdentifiers.sha256WithRSAEncryption.getId(), "SHA256");
        digestNames.put(PKCSObjectIdentifiers.sha384WithRSAEncryption.getId(), "SHA384");
        digestNames.put(PKCSObjectIdentifiers.sha512WithRSAEncryption.getId(), "SHA512");
        digestNames.put(TeleTrusTObjectIdentifiers.ripemd128.getId(), "RIPEMD128");
        digestNames.put(TeleTrusTObjectIdentifiers.ripemd160.getId(), "RIPEMD160");
        digestNames.put(TeleTrusTObjectIdentifiers.ripemd256.getId(), "RIPEMD256");
        digestNames.put(CryptoProObjectIdentifiers.gostR3411.getId(), "GOST3411");
    }

    /**
     * Fetches the signature time-stamp attributes from a SignerInformation object.
     * Checks that the MessageImprint for each time-stamp matches the signature field.
     * (see RFC 3161 Appendix A).
     * 
     * @param signerInfo a SignerInformation to search for time-stamps
     * @param provider an optional provider to use to create MessageDigest instances
     * @return a collection of TimeStampToken objects
     * @throws TSPValidationException
     * @deprecated use getSignatureTimestamps(SignerInformation, DigestCalculatorProvider)
     */
    public static Collection getSignatureTimestamps(SignerInformation signerInfo, Provider provider)
        throws TSPValidationException
    {
        List timestamps = new ArrayList();

        AttributeTable unsignedAttrs = signerInfo.getUnsignedAttributes();
        if (unsignedAttrs != null)
        {
            ASN1EncodableVector allTSAttrs = unsignedAttrs.getAll(
                PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
            for (int i = 0; i < allTSAttrs.size(); ++i)
            {
                Attribute tsAttr = (Attribute)allTSAttrs.get(i);            
                ASN1Set tsAttrValues = tsAttr.getAttrValues();
                for (int j = 0; j < tsAttrValues.size(); ++j)
                {
                    try
                    {
                        ContentInfo contentInfo = ContentInfo.getInstance(tsAttrValues.getObjectAt(j).getDERObject());
                        TimeStampToken timeStampToken = new TimeStampToken(contentInfo);
                        TimeStampTokenInfo tstInfo = timeStampToken.getTimeStampInfo();

                        MessageDigest digest = createDigestInstance(tstInfo.getMessageImprintAlgOID(), provider);
                        byte[] expectedDigest = digest.digest(signerInfo.getSignature());

                        if (!Arrays.constantTimeAreEqual(expectedDigest, tstInfo.getMessageImprintDigest()))
                        {
                            throw new TSPValidationException("Incorrect digest in message imprint");
                        }

                        timestamps.add(timeStampToken);
                    }
                    catch (NoSuchAlgorithmException e)
                    {
                        throw new TSPValidationException("Unknown hash algorithm specified in timestamp");
                    }
                    catch (Exception e)
                    {
                        throw new TSPValidationException("Timestamp could not be parsed");
                    }
                }
            }
        }

        return timestamps;
    }

     /**
     * Fetches the signature time-stamp attributes from a SignerInformation object.
     * Checks that the MessageImprint for each time-stamp matches the signature field.
     * (see RFC 3161 Appendix A).
     *
     * @param signerInfo a SignerInformation to search for time-stamps
     * @param digCalcProvider provider for digest calculators
     * @return a collection of TimeStampToken objects
     * @throws TSPValidationException
     */
    public static Collection getSignatureTimestamps(SignerInformation signerInfo, DigestCalculatorProvider digCalcProvider)
        throws TSPValidationException
    {
        List timestamps = new ArrayList();

        AttributeTable unsignedAttrs = signerInfo.getUnsignedAttributes();
        if (unsignedAttrs != null)
        {
            ASN1EncodableVector allTSAttrs = unsignedAttrs.getAll(
                PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
            for (int i = 0; i < allTSAttrs.size(); ++i)
            {
                Attribute tsAttr = (Attribute)allTSAttrs.get(i);
                ASN1Set tsAttrValues = tsAttr.getAttrValues();
                for (int j = 0; j < tsAttrValues.size(); ++j)
                {
                    try
                    {
                        ContentInfo contentInfo = ContentInfo.getInstance(tsAttrValues.getObjectAt(j).getDERObject());
                        TimeStampToken timeStampToken = new TimeStampToken(contentInfo);
                        TimeStampTokenInfo tstInfo = timeStampToken.getTimeStampInfo();

                        DigestCalculator digCalc = digCalcProvider.get(tstInfo.getHashAlgorithm());

                        OutputStream dOut = digCalc.getOutputStream();

                        dOut.write(signerInfo.getSignature());
                        dOut.close();

                        byte[] expectedDigest = digCalc.getDigest();

                        if (!Arrays.constantTimeAreEqual(expectedDigest, tstInfo.getMessageImprintDigest()))
                        {
                            throw new TSPValidationException("Incorrect digest in message imprint");
                        }

                        timestamps.add(timeStampToken);
                    }
                    catch (OperatorCreationException e)
                    {
                        throw new TSPValidationException("Unknown hash algorithm specified in timestamp");
                    }
                    catch (Exception e)
                    {
                        throw new TSPValidationException("Timestamp could not be parsed");
                    }
                }
            }
        }

        return timestamps;
    }

    /**
     * Validate the passed in certificate as being of the correct type to be used
     * for time stamping. To be valid it must have an ExtendedKeyUsage extension
     * which has a key purpose identifier of id-kp-timeStamping.
     * 
     * @param cert the certificate of interest.
     * @throws TSPValidationException if the certicate fails on one of the check points.
     */
    public static void validateCertificate(
        X509Certificate cert)
        throws TSPValidationException
    {
        if (cert.getVersion() != 3)
        {
            throw new IllegalArgumentException("Certificate must have an ExtendedKeyUsage extension.");
        }
        
        byte[]  ext = cert.getExtensionValue(X509Extensions.ExtendedKeyUsage.getId());
        if (ext == null)
        {
            throw new TSPValidationException("Certificate must have an ExtendedKeyUsage extension.");
        }
        
        if (!cert.getCriticalExtensionOIDs().contains(X509Extensions.ExtendedKeyUsage.getId()))
        {
            throw new TSPValidationException("Certificate must have an ExtendedKeyUsage extension marked as critical.");
        }

        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(ext));

        try
        {
            aIn = new ASN1InputStream(new ByteArrayInputStream(((ASN1OctetString)aIn.readObject()).getOctets()));
            
            ExtendedKeyUsage    extKey = ExtendedKeyUsage.getInstance(aIn.readObject());
            
            if (!extKey.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping) || extKey.size() != 1)
            {
                throw new TSPValidationException("ExtendedKeyUsage not solely time stamping.");
            }
        }
        catch (IOException e)
        {
            throw new TSPValidationException("cannot process ExtendedKeyUsage extension");
        }
    }

    /**
     * Validate the passed in certificate as being of the correct type to be used
     * for time stamping. To be valid it must have an ExtendedKeyUsage extension
     * which has a key purpose identifier of id-kp-timeStamping.
     *
     * @param cert the certificate of interest.
     * @throws TSPValidationException if the certicate fails on one of the check points.
     */
    public static void validateCertificate(
        X509CertificateHolder cert)
        throws TSPValidationException
    {
        if (cert.toASN1Structure().getVersion() != 3)
        {
            throw new IllegalArgumentException("Certificate must have an ExtendedKeyUsage extension.");
        }

        X509Extension  ext = cert.getExtension(X509Extension.extendedKeyUsage);
        if (ext == null)
        {
            throw new TSPValidationException("Certificate must have an ExtendedKeyUsage extension.");
        }

        if (!ext.isCritical())
        {
            throw new TSPValidationException("Certificate must have an ExtendedKeyUsage extension marked as critical.");
        }

        ExtendedKeyUsage    extKey = ExtendedKeyUsage.getInstance(X509Extension.convertValueToObject(ext));

        if (!extKey.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping) || extKey.size() != 1)
        {
            throw new TSPValidationException("ExtendedKeyUsage not solely time stamping.");
        }
    }

    /*
     * Return the digest algorithm using one of the standard JCA string
     * representations rather than the algorithm identifier (if possible).
     */
    static String getDigestAlgName(
        String digestAlgOID)
    {
        String digestName = (String)digestNames.get(digestAlgOID);

        if (digestName != null)
        {
            return digestName;
        }

        return digestAlgOID;
    }

    static int getDigestLength(
        String digestAlgOID)
        throws TSPException
    {
        Integer length = (Integer)digestLengths.get(digestAlgOID);

        if (length != null)
        {
            return length.intValue();
        }

        throw new TSPException("digest algorithm cannot be found.");
    }

    static MessageDigest createDigestInstance(String digestAlgOID, Provider provider)
        throws NoSuchAlgorithmException
    {
        String digestName = TSPUtil.getDigestAlgName(digestAlgOID);

        if (provider != null)
        {
            try
            {
                return MessageDigest.getInstance(digestName, provider);
            }
            catch (NoSuchAlgorithmException e)
            {
                // Ignore
            }
        }

        return MessageDigest.getInstance(digestName);
    }

        static Set getCriticalExtensionOIDs(X509Extensions extensions)
    {
        if (extensions == null)
        {
            return EMPTY_SET;
        }

        return Collections.unmodifiableSet(new HashSet(java.util.Arrays.asList(extensions.getCriticalExtensionOIDs())));
    }

    static Set getNonCriticalExtensionOIDs(X509Extensions extensions)
    {
        if (extensions == null)
        {
            return EMPTY_SET;
        }

        // TODO: should probably produce a set that imposes correct ordering
        return Collections.unmodifiableSet(new HashSet(java.util.Arrays.asList(extensions.getNonCriticalExtensionOIDs())));
    }

    static List getExtensionOIDs(X509Extensions extensions)
    {
        if (extensions == null)
        {
            return EMPTY_LIST;
        }

        return Collections.unmodifiableList(java.util.Arrays.asList(extensions.getExtensionOIDs()));
    }
}
