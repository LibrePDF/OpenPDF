package old.org.bouncycastle.cert.cmp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERGeneralizedTime;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.cmp.CMPCertificate;
import old.org.bouncycastle.asn1.cmp.InfoTypeAndValue;
import old.org.bouncycastle.asn1.cmp.PKIBody;
import old.org.bouncycastle.asn1.cmp.PKIFreeText;
import old.org.bouncycastle.asn1.cmp.PKIHeader;
import old.org.bouncycastle.asn1.cmp.PKIHeaderBuilder;
import old.org.bouncycastle.asn1.cmp.PKIMessage;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.operator.ContentSigner;
import old.org.bouncycastle.operator.MacCalculator;

/**
 * Builder for creating a protected PKI message.
 */
public class ProtectedPKIMessageBuilder
{
    private PKIHeaderBuilder hdrBuilder;
    private PKIBody body;
    private List generalInfos = new ArrayList();
    private List extraCerts = new ArrayList();

    /**
     * Commence a message with the header version CMP_2000.
     *
     * @param sender message sender.
     * @param recipient intended recipient.
     */
    public ProtectedPKIMessageBuilder(GeneralName sender, GeneralName recipient)
    {
        this(PKIHeader.CMP_2000, sender, recipient);
    }

    /**
     * Commence a message with a specific header type.
     *
     * @param pvno  the version CMP_1999 or CMP_2000.
     * @param sender message sender.
     * @param recipient intended recipient.
     */
    public ProtectedPKIMessageBuilder(int pvno, GeneralName sender, GeneralName recipient)
    {
        hdrBuilder = new PKIHeaderBuilder(pvno, sender, recipient);
    }

    /**
     * Set the identifier for the transaction the new message will belong to.
     *
     * @param tid  the transaction ID.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setTransactionID(byte[] tid)
    {
        hdrBuilder.setTransactionID(tid);

        return this;
    }

    /**
     * Include a human-readable message in the new message.
     *
     * @param freeText the contents of the human readable message,
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setFreeText(PKIFreeText freeText)
    {
        hdrBuilder.setFreeText(freeText);

        return this;
    }

    /**
     * Add a generalInfo data record to the header of the new message.
     *
     * @param genInfo the generalInfo data to be added.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder addGeneralInfo(InfoTypeAndValue genInfo)
    {
        generalInfos.add(genInfo);

        return this;
    }

    /**
     * Set the creation time for the new message.
     *
     * @param time the message creation time.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setMessageTime(Date time)
    {
        hdrBuilder.setMessageTime(new DERGeneralizedTime(time));

        return this;
    }

    /**
     * Set the recipient key identifier for the key to be used to verify the new message.
     *
     * @param kid a key identifier.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setRecipKID(byte[] kid)
    {
        hdrBuilder.setRecipKID(kid);

        return this;
    }

    /**
     * Set the recipient nonce field on the new message.
     *
     * @param nonce a NONCE, typically copied from the sender nonce of the previous message.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setRecipNonce(byte[] nonce)
    {
        hdrBuilder.setRecipNonce(nonce);

        return this;
    }

    /**
     * Set the sender key identifier for the key used to protect the new message.
     *
     * @param kid a key identifier.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setSenderKID(byte[] kid)
    {
        hdrBuilder.setSenderKID(kid);

        return this;
    }

    /**
     * Set the sender nonce field on the new message.
     *
     * @param nonce a NONCE, typically 128 bits of random data.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setSenderNonce(byte[] nonce)
    {
        hdrBuilder.setSenderNonce(nonce);

        return this;
    }

    /**
     * Set the body for the new message
     *
     * @param body the message body.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder setBody(PKIBody body)
    {
        this.body = body;

        return this;
    }

    /**
     * Add an "extra certificate" to the message.
     *
     * @param extraCert the extra certificate to add.
     * @return the current builder instance.
     */
    public ProtectedPKIMessageBuilder addCMPCertificate(X509CertificateHolder extraCert)
    {
        extraCerts.add(extraCert);

        return this;
    }

    /**
     * Build a protected PKI message which has MAC based integrity protection.
     *
     * @param macCalculator MAC calculator.
     * @return the resulting protected PKI message.
     * @throws CMPException if the protection MAC cannot be calculated.
     */
    public ProtectedPKIMessage build(MacCalculator macCalculator)
        throws CMPException
    {
        finaliseHeader(macCalculator.getAlgorithmIdentifier());

        PKIHeader header = hdrBuilder.build();

        try
        {
            DERBitString protection = new DERBitString(calculateMac(macCalculator, header, body));

            return finaliseMessage(header, protection);
        }
        catch (IOException e)
        {
            throw new CMPException("unable to encode MAC input: " + e.getMessage(), e);
        }
    }

    /**
     * Build a protected PKI message which has MAC based integrity protection.
     *
     * @param signer the ContentSigner to be used to calculate the signature.
     * @return the resulting protected PKI message.
     * @throws CMPException if the protection signature cannot be calculated.
     */
    public ProtectedPKIMessage build(ContentSigner signer)
        throws CMPException
    {
        finaliseHeader(signer.getAlgorithmIdentifier());

        PKIHeader header = hdrBuilder.build();
        
        try
        {
            DERBitString protection = new DERBitString(calculateSignature(signer, header, body));

            return finaliseMessage(header, protection);
        }
        catch (IOException e)
        {
            throw new CMPException("unable to encode signature input: " + e.getMessage(), e);
        }
    }

    private void finaliseHeader(AlgorithmIdentifier algorithmIdentifier)
    {
        hdrBuilder.setProtectionAlg(algorithmIdentifier);

        if (!generalInfos.isEmpty())
        {
            InfoTypeAndValue[] genInfos = new InfoTypeAndValue[generalInfos.size()];

            hdrBuilder.setGeneralInfo((InfoTypeAndValue[])generalInfos.toArray(genInfos));
        }
    }

    private ProtectedPKIMessage finaliseMessage(PKIHeader header, DERBitString protection)
    {
        if (!extraCerts.isEmpty())
        {
            CMPCertificate[] cmpCerts = new CMPCertificate[extraCerts.size()];

            for (int i = 0; i != cmpCerts.length; i++)
            {
                cmpCerts[i] = new CMPCertificate(((X509CertificateHolder)extraCerts.get(i)).toASN1Structure());
            }

            return new ProtectedPKIMessage(new PKIMessage(header, body, protection, cmpCerts));
        }
        else
        {
            return new ProtectedPKIMessage(new PKIMessage(header, body, protection));
        }
    }

    private byte[] calculateSignature(ContentSigner signer, PKIHeader header, PKIBody body)
        throws IOException
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(header);
        v.add(body);

        OutputStream sOut = signer.getOutputStream();

        sOut.write(new DERSequence(v).getDEREncoded());

        sOut.close();

        return signer.getSignature();
    }

    private byte[] calculateMac(MacCalculator macCalculator, PKIHeader header, PKIBody body)
        throws IOException
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(header);
        v.add(body);

        OutputStream sOut = macCalculator.getOutputStream();

        sOut.write(new DERSequence(v).getDEREncoded());

        sOut.close();

        return macCalculator.getMac();
    }
}
