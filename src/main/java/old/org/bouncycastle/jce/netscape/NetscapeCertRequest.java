package old.org.bouncycastle.jce.netscape;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 *
 * 
 * Handles NetScape certificate request (KEYGEN), these are constructed as:
 * <pre><code>
 *   SignedPublicKeyAndChallenge ::= SEQUENCE {
 *     publicKeyAndChallenge    PublicKeyAndChallenge,
 *     signatureAlgorithm       AlgorithmIdentifier,
 *     signature                BIT STRING
 *   }
 * </pre>
 *
 * PublicKey's encoded-format has to be X.509.
 *
 **/
public class NetscapeCertRequest
    extends ASN1Encodable
{
    AlgorithmIdentifier    sigAlg;
    AlgorithmIdentifier    keyAlg;
    byte        sigBits [];
    String challenge;
    DERBitString content;
    PublicKey pubkey ;
    
    private static ASN1Sequence getReq(
        byte[]  r)
        throws IOException
    {
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(r));

        return ASN1Sequence.getInstance(aIn.readObject());
    }

    public NetscapeCertRequest(
        byte[]  req)
        throws IOException
    {
        this(getReq(req));
    }

    public NetscapeCertRequest (ASN1Sequence spkac)
    {
        try
        {

            //
            // SignedPublicKeyAndChallenge ::= SEQUENCE {
            //    publicKeyAndChallenge    PublicKeyAndChallenge,
            //    signatureAlgorithm    AlgorithmIdentifier,
            //    signature        BIT STRING
            // }
            //
            if (spkac.size() != 3)
            {
                throw new IllegalArgumentException("invalid SPKAC (size):"
                        + spkac.size());
            }

            sigAlg = new AlgorithmIdentifier((ASN1Sequence)spkac
                    .getObjectAt(1));
            sigBits = ((DERBitString)spkac.getObjectAt(2)).getBytes();

            //
            // PublicKeyAndChallenge ::= SEQUENCE {
            //    spki            SubjectPublicKeyInfo,
            //    challenge        IA5STRING
            // }
            //
            ASN1Sequence pkac = (ASN1Sequence)spkac.getObjectAt(0);

            if (pkac.size() != 2)
            {
                throw new IllegalArgumentException("invalid PKAC (len): "
                        + pkac.size());
            }

            challenge = ((DERIA5String)pkac.getObjectAt(1)).getString();

            //this could be dangerous, as ASN.1 decoding/encoding
            //could potentially alter the bytes
            content = new DERBitString(pkac);

            SubjectPublicKeyInfo pubkeyinfo = new SubjectPublicKeyInfo(
                    (ASN1Sequence)pkac.getObjectAt(0));

            X509EncodedKeySpec xspec = new X509EncodedKeySpec(new DERBitString(
                    pubkeyinfo).getBytes());

            keyAlg = pubkeyinfo.getAlgorithmId();
            pubkey = KeyFactory.getInstance(keyAlg.getObjectId().getId(), "BC")
                    .generatePublic(xspec);

        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public NetscapeCertRequest(
        String challenge,
        AlgorithmIdentifier signing_alg,
        PublicKey pub_key) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException
    {

        this.challenge = challenge;
        sigAlg = signing_alg;
        pubkey = pub_key;

        ASN1EncodableVector content_der = new ASN1EncodableVector();
        content_der.add(getKeySpec());
        //content_der.add(new SubjectPublicKeyInfo(sigAlg, new RSAPublicKeyStructure(pubkey.getModulus(), pubkey.getPublicExponent()).getDERObject()));
        content_der.add(new DERIA5String(challenge));

        content = new DERBitString(new DERSequence(content_der));
    }

    public String getChallenge()
    {
        return challenge;
    }

    public void setChallenge(String value)
    {
        challenge = value;
    }

    public AlgorithmIdentifier getSigningAlgorithm()
    {
        return sigAlg;
    }

    public void setSigningAlgorithm(AlgorithmIdentifier value)
    {
        sigAlg = value;
    }

    public AlgorithmIdentifier getKeyAlgorithm()
    {
        return keyAlg;
    }

    public void setKeyAlgorithm(AlgorithmIdentifier value)
    {
        keyAlg = value;
    }

    public PublicKey getPublicKey()
    {
        return pubkey;
    }

    public void setPublicKey(PublicKey value)
    {
        pubkey = value;
    }

    public boolean verify(String challenge) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException, NoSuchProviderException
    {
        if (!challenge.equals(this.challenge))
        {
            return false;
        }

        //
        // Verify the signature .. shows the response was generated
        // by someone who knew the associated private key
        //
        Signature sig = Signature.getInstance(sigAlg.getObjectId().getId(),
                "BC");
        sig.initVerify(pubkey);
        sig.update(content.getBytes());

        return sig.verify(sigBits);
    }

    public void sign(PrivateKey priv_key) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException, NoSuchProviderException,
            InvalidKeySpecException
    {
        sign(priv_key, null);
    }

    public void sign(PrivateKey priv_key, SecureRandom rand)
            throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, NoSuchProviderException,
            InvalidKeySpecException
    {
        Signature sig = Signature.getInstance(sigAlg.getObjectId().getId(),
                "BC");

        if (rand != null)
        {
            sig.initSign(priv_key, rand);
        }
        else
        {
            sig.initSign(priv_key);
        }

        ASN1EncodableVector pkac = new ASN1EncodableVector();

        pkac.add(getKeySpec());
        pkac.add(new DERIA5String(challenge));

        try
        {
            sig.update(new DERSequence(pkac).getEncoded(ASN1Encodable.DER));
        }
        catch (IOException ioe)
        {
            throw new SignatureException(ioe.getMessage());
        }

        sigBits = sig.sign();
    }

    private DERObject getKeySpec() throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        DERObject obj = null;
        try
        {

            baos.write(pubkey.getEncoded());
            baos.close();

            ASN1InputStream derin = new ASN1InputStream(
                    new ByteArrayInputStream(baos.toByteArray()));

            obj = derin.readObject();
        }
        catch (IOException ioe)
        {
            throw new InvalidKeySpecException(ioe.getMessage());
        }
        return obj;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector spkac = new ASN1EncodableVector();
        ASN1EncodableVector pkac = new ASN1EncodableVector();

        try
        {
            pkac.add(getKeySpec());
        }
        catch (Exception e)
        {
            //ignore
        }

        pkac.add(new DERIA5String(challenge));

        spkac.add(new DERSequence(pkac));
        spkac.add(sigAlg);
        spkac.add(new DERBitString(sigBits));

        return new DERSequence(spkac);
    }
}
