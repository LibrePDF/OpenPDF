package old.org.bouncycastle.cert;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.AttributeCertificate;
import old.org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import old.org.bouncycastle.asn1.x509.CertificateList;
import old.org.bouncycastle.asn1.x509.TBSCertList;
import old.org.bouncycastle.asn1.x509.TBSCertificateStructure;
import old.org.bouncycastle.asn1.x509.X509CertificateStructure;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.operator.ContentSigner;

class CertUtils
{
    private static Set EMPTY_SET = Collections.unmodifiableSet(new HashSet());
    private static List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());

    static X509CertificateHolder generateFullCert(ContentSigner signer, TBSCertificateStructure tbsCert)
    {
        try
        {
            return new X509CertificateHolder(generateStructure(tbsCert, signer.getAlgorithmIdentifier(), generateSig(signer, tbsCert)));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("cannot produce certificate signature");
        }
    }

    static X509AttributeCertificateHolder generateFullAttrCert(ContentSigner signer, AttributeCertificateInfo attrInfo)
    {
        try
        {
            return new X509AttributeCertificateHolder(generateAttrStructure(attrInfo, signer.getAlgorithmIdentifier(), generateSig(signer, attrInfo)));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("cannot produce attribute certificate signature");
        }
    }

    static X509CRLHolder generateFullCRL(ContentSigner signer, TBSCertList tbsCertList)
    {
        try
        {
            return new X509CRLHolder(generateCRLStructure(tbsCertList, signer.getAlgorithmIdentifier(), generateSig(signer, tbsCertList)));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("cannot produce certificate signature");
        }
    }

    private static byte[] generateSig(ContentSigner signer, ASN1Encodable tbsObj)
        throws IOException
    {
        OutputStream sOut = signer.getOutputStream();

        sOut.write(tbsObj.getDEREncoded());

        sOut.close();

        return signer.getSignature();
    }

    private static X509CertificateStructure generateStructure(TBSCertificateStructure tbsCert, AlgorithmIdentifier sigAlgId, byte[] signature)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCert);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        return X509CertificateStructure.getInstance(new DERSequence(v));
    }

    private static AttributeCertificate generateAttrStructure(AttributeCertificateInfo attrInfo, AlgorithmIdentifier sigAlgId, byte[] signature)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(attrInfo);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        return AttributeCertificate.getInstance(new DERSequence(v));
    }

    private static CertificateList generateCRLStructure(TBSCertList tbsCertList, AlgorithmIdentifier sigAlgId, byte[] signature)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(tbsCertList);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        return CertificateList.getInstance(new DERSequence(v));
    }

    static Set getCriticalExtensionOIDs(X509Extensions extensions)
    {
        if (extensions == null)
        {
            return EMPTY_SET;
        }

        return Collections.unmodifiableSet(new HashSet(Arrays.asList(extensions.getCriticalExtensionOIDs())));
    }

    static Set getNonCriticalExtensionOIDs(X509Extensions extensions)
    {
        if (extensions == null)
        {
            return EMPTY_SET;
        }

        // TODO: should probably produce a set that imposes correct ordering
        return Collections.unmodifiableSet(new HashSet(Arrays.asList(extensions.getNonCriticalExtensionOIDs())));
    }

    static List getExtensionOIDs(X509Extensions extensions)
    {
        if (extensions == null)
        {
            return EMPTY_LIST;
        }

        return Collections.unmodifiableList(Arrays.asList(extensions.getExtensionOIDs()));
    }

    static DERBitString booleanToBitString(boolean[] id)
    {
        byte[] bytes = new byte[(id.length + 7) / 8];

        for (int i = 0; i != id.length; i++)
        {
            bytes[i / 8] |= (id[i]) ? (1 << ((7 - (i % 8)))) : 0;
        }

        int pad = id.length % 8;

        if (pad == 0)
        {
            return new DERBitString(bytes);
        }
        else
        {
            return new DERBitString(bytes, 8 - pad);
        }
    }

    static boolean[] bitStringToBoolean(DERBitString bitString)
    {
        if (bitString != null)
        {
            byte[]          bytes = bitString.getBytes();
            boolean[]       boolId = new boolean[bytes.length * 8 - bitString.getPadBits()];

            for (int i = 0; i != boolId.length; i++)
            {
                boolId[i] = (bytes[i / 8] & (0x80 >>> (i % 8))) != 0;
            }

            return boolId;
        }

        return null;
    }

    static Date recoverDate(DERGeneralizedTime time)
    {
        try
        {
            return time.getDate();
        }
        catch (ParseException e)
        {
            throw new IllegalStateException("unable to recover date: " + e.getMessage());
        }
    }
}
