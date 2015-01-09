package old.org.bouncycastle.cert.crmf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Integer;
import old.org.bouncycastle.asn1.ASN1Null;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.crmf.AttributeTypeAndValue;
import old.org.bouncycastle.asn1.crmf.CertReqMsg;
import old.org.bouncycastle.asn1.crmf.CertRequest;
import old.org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import old.org.bouncycastle.asn1.crmf.POPOPrivKey;
import old.org.bouncycastle.asn1.crmf.ProofOfPossession;
import old.org.bouncycastle.asn1.crmf.SubsequentMessage;
import old.org.bouncycastle.asn1.x500.X500Name;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509ExtensionsGenerator;
import old.org.bouncycastle.operator.ContentSigner;

public class CertificateRequestMessageBuilder
{
    private final BigInteger certReqId;

    private X509ExtensionsGenerator extGenerator;
    private CertTemplateBuilder templateBuilder;
    private List controls;
    private ContentSigner popSigner;
    private PKMACBuilder pkmacBuilder;
    private char[] password;
    private GeneralName sender;
    private POPOPrivKey popoPrivKey;
    private ASN1Null popRaVerified;

    public CertificateRequestMessageBuilder(BigInteger certReqId)
    {
        this.certReqId = certReqId;

        this.extGenerator = new X509ExtensionsGenerator();
        this.templateBuilder = new CertTemplateBuilder();
        this.controls = new ArrayList();
    }

    public CertificateRequestMessageBuilder setPublicKey(SubjectPublicKeyInfo publicKey)
    {
        if (publicKey != null)
        {
            templateBuilder.setPublicKey(publicKey);
        }

        return this;
    }

    public CertificateRequestMessageBuilder setIssuer(X500Name issuer)
    {
        if (issuer != null)
        {
            templateBuilder.setIssuer(issuer);
        }

        return this;
    }

    public CertificateRequestMessageBuilder setSubject(X500Name subject)
    {
        if (subject != null)
        {
            templateBuilder.setSubject(subject);
        }

        return this;
    }

    public CertificateRequestMessageBuilder setSerialNumber(BigInteger serialNumber)
    {
        if (serialNumber != null)
        {
            templateBuilder.setSerialNumber(new ASN1Integer(serialNumber));
        }

        return this;
    }

    public CertificateRequestMessageBuilder addExtension(
        ASN1ObjectIdentifier oid,
        boolean              critical,
        ASN1Encodable        value)
    {
        extGenerator.addExtension(oid, critical,  value);

        return this;
    }

    public CertificateRequestMessageBuilder addExtension(
        ASN1ObjectIdentifier oid,
        boolean              critical,
        byte[]               value)
    {
        extGenerator.addExtension(oid, critical, value);

        return this;
    }

    public CertificateRequestMessageBuilder addControl(Control control)
    {
        controls.add(control);

        return this;
    }

    public CertificateRequestMessageBuilder setProofOfPossessionSigningKeySigner(ContentSigner popSigner)
    {
        if (popoPrivKey != null || popRaVerified != null)
        {
            throw new IllegalStateException("only one proof of possession allowed");
        }

        this.popSigner = popSigner;

        return this;
    }

    public CertificateRequestMessageBuilder setProofOfPossessionSubsequentMessage(SubsequentMessage msg)
    {
        if (popSigner != null || popRaVerified != null)
        {
            throw new IllegalStateException("only one proof of possession allowed");
        }

        this.popoPrivKey = new POPOPrivKey(msg);

        return this;
    }

    public CertificateRequestMessageBuilder setProofOfPossessionRaVerified()
    {
        if (popSigner != null || popoPrivKey != null)
        {
            throw new IllegalStateException("only one proof of possession allowed");
        }

        this.popRaVerified = DERNull.INSTANCE;

        return this;
    }

    public CertificateRequestMessageBuilder setAuthInfoPKMAC(PKMACBuilder pkmacBuilder, char[] password)
    {
        this.pkmacBuilder = pkmacBuilder;
        this.password = password;

        return this;
    }

    public CertificateRequestMessageBuilder setAuthInfoSender(X500Name sender)
    {
        return setAuthInfoSender(new GeneralName(sender));
    }

    public CertificateRequestMessageBuilder setAuthInfoSender(GeneralName sender)
    {
        this.sender = sender;

        return this;
    }

    public CertificateRequestMessage build()
        throws CRMFException
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(new DERInteger(certReqId));

        if (!extGenerator.isEmpty())
        {
            templateBuilder.setExtensions(extGenerator.generate());
        }

        v.add(templateBuilder.build());

        if (!controls.isEmpty())
        {
            ASN1EncodableVector controlV = new ASN1EncodableVector();

            for (Iterator it = controls.iterator(); it.hasNext();)
            {
                Control control = (Control)it.next();

                controlV.add(new AttributeTypeAndValue(control.getType(), control.getValue()));
            }

            v.add(new DERSequence(controlV));
        }

        CertRequest request = CertRequest.getInstance(new DERSequence(v));

        v = new ASN1EncodableVector();

        v.add(request);

        if (popSigner != null)
        {
            SubjectPublicKeyInfo pubKeyInfo = request.getCertTemplate().getPublicKey();
            ProofOfPossessionSigningKeyBuilder builder = new ProofOfPossessionSigningKeyBuilder(pubKeyInfo);

            if (sender != null)
            {
                builder.setSender(sender);
            }
            else
            {
                PKMACValueGenerator pkmacGenerator = new PKMACValueGenerator(pkmacBuilder);

                builder.setPublicKeyMac(pkmacGenerator, password);
            }

            v.add(new ProofOfPossession(builder.build(popSigner)));
        }
        else if (popoPrivKey != null)
        {
            v.add(new ProofOfPossession(ProofOfPossession.TYPE_KEY_ENCIPHERMENT, popoPrivKey));
        }
        else if (popRaVerified != null)
        {
            v.add(new ProofOfPossession());
        }

        return new CertificateRequestMessage(CertReqMsg.getInstance(new DERSequence(v)));
    }
}