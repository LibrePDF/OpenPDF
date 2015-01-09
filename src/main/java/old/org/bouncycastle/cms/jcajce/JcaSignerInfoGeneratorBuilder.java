package old.org.bouncycastle.cms.jcajce;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import old.org.bouncycastle.cert.X509CertificateHolder;
import old.org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import old.org.bouncycastle.cms.CMSAttributeTableGenerator;
import old.org.bouncycastle.cms.SignerInfoGenerator;
import old.org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import old.org.bouncycastle.operator.ContentSigner;
import old.org.bouncycastle.operator.DigestCalculatorProvider;
import old.org.bouncycastle.operator.OperatorCreationException;

public class JcaSignerInfoGeneratorBuilder
{
    private SignerInfoGeneratorBuilder builder;

    public JcaSignerInfoGeneratorBuilder(DigestCalculatorProvider digestProvider)
    {
        builder = new SignerInfoGeneratorBuilder(digestProvider);
    }

    /**
     * If the passed in flag is true, the signer signature will be based on the data, not
     * a collection of signed attributes, and no signed attributes will be included.
     *
     * @return the builder object
     */
    public JcaSignerInfoGeneratorBuilder setDirectSignature(boolean hasNoSignedAttributes)
    {
        builder.setDirectSignature(hasNoSignedAttributes);

        return this;
    }

    public JcaSignerInfoGeneratorBuilder setSignedAttributeGenerator(CMSAttributeTableGenerator signedGen)
    {
        builder.setSignedAttributeGenerator(signedGen);

        return this;
    }

    public JcaSignerInfoGeneratorBuilder setUnsignedAttributeGenerator(CMSAttributeTableGenerator unsignedGen)
    {
        builder.setUnsignedAttributeGenerator(unsignedGen);

        return this;
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, X509CertificateHolder certHolder)
        throws OperatorCreationException
    {
        return builder.build(contentSigner, certHolder);
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, byte[] keyIdentifier)
        throws OperatorCreationException
    {
        return builder.build(contentSigner, keyIdentifier);
    }

    public SignerInfoGenerator build(ContentSigner contentSigner, X509Certificate certificate)
        throws OperatorCreationException, CertificateEncodingException
    {
        return this.build(contentSigner, new JcaX509CertificateHolder(certificate));
    }
}
