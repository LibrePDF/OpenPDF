package old.org.bouncycastle.jce.provider;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.BERConstructedOctetString;
import old.org.bouncycastle.asn1.BEROutputStream;
import old.org.bouncycastle.asn1.DERBMPString;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DEROutputStream;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERSet;
import old.org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import old.org.bouncycastle.asn1.pkcs.CertBag;
import old.org.bouncycastle.asn1.pkcs.ContentInfo;
import old.org.bouncycastle.asn1.pkcs.EncryptedData;
import old.org.bouncycastle.asn1.pkcs.MacData;
import old.org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.Pfx;
import old.org.bouncycastle.asn1.pkcs.SafeBag;
import old.org.bouncycastle.asn1.util.ASN1Dump;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import old.org.bouncycastle.asn1.x509.DigestInfo;
import old.org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x509.X509Extensions;
import old.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import old.org.bouncycastle.jce.interfaces.BCKeyStore;
import old.org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import old.org.bouncycastle.util.Arrays;
import old.org.bouncycastle.util.Strings;
import old.org.bouncycastle.util.encoders.Hex;

public class JDKPKCS12KeyStore
    extends KeyStoreSpi
    implements PKCSObjectIdentifiers, X509ObjectIdentifiers, BCKeyStore
{
    private static final int                SALT_SIZE = 20;
    private static final int                MIN_ITERATIONS = 1024;

    private static final Provider           bcProvider = new BouncyCastleProvider();

    private IgnoresCaseHashtable            keys = new IgnoresCaseHashtable();
    private Hashtable                       localIds = new Hashtable();
    private IgnoresCaseHashtable            certs = new IgnoresCaseHashtable();
    private Hashtable                       chainCerts = new Hashtable();
    private Hashtable                       keyCerts = new Hashtable();

    //
    // generic object types
    //
    static final int NULL           = 0;
    static final int CERTIFICATE    = 1;
    static final int KEY            = 2;
    static final int SECRET         = 3;
    static final int SEALED         = 4;

    //
    // key types
    //
    static final int    KEY_PRIVATE = 0;
    static final int    KEY_PUBLIC  = 1;
    static final int    KEY_SECRET  = 2;

    protected SecureRandom      random = new SecureRandom();

    // use of final causes problems with JDK 1.2 compiler
    private CertificateFactory  certFact;
    private DERObjectIdentifier keyAlgorithm;
    private DERObjectIdentifier certAlgorithm;

    private class CertId
    {
        byte[]  id;

        CertId(
            PublicKey  key)
        {
            this.id = createSubjectKeyId(key).getKeyIdentifier();
        }

        CertId(
            byte[]  id)
        {
            this.id = id;
        }

        public int hashCode()
        {
            return Arrays.hashCode(id);
        }

        public boolean equals(
            Object  o)
        {
            if (o == this)
            {
                return true;
            }

            if (!(o instanceof CertId))
            {
                return false;
            }

            CertId  cId = (CertId)o;

            return Arrays.areEqual(id, cId.id);
        }
    }

    public JDKPKCS12KeyStore(
        Provider provider,
        DERObjectIdentifier keyAlgorithm,
        DERObjectIdentifier certAlgorithm)
    {
        this.keyAlgorithm = keyAlgorithm;
        this.certAlgorithm = certAlgorithm;

        try
        {
            if (provider != null)
            {
                certFact = CertificateFactory.getInstance("X.509", provider);
            }
            else
            {
                certFact = CertificateFactory.getInstance("X.509");
            }
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("can't create cert factory - " + e.toString());
        }
    }

    private SubjectKeyIdentifier createSubjectKeyId(
        PublicKey   pubKey)
    {
        try
        {
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                (ASN1Sequence) ASN1Object.fromByteArray(pubKey.getEncoded()));

            return new SubjectKeyIdentifier(info);
        }
        catch (Exception e)
        {
            throw new RuntimeException("error creating key");
        }
    }

    public void setRandom(
        SecureRandom    rand)
    {
        this.random = rand;
    }

    public Enumeration engineAliases() 
    {
        Hashtable  tab = new Hashtable();

        Enumeration e = certs.keys();
        while (e.hasMoreElements())
        {
            tab.put(e.nextElement(), "cert");
        }

        e = keys.keys();
        while (e.hasMoreElements())
        {
            String  a = (String)e.nextElement();
            if (tab.get(a) == null)
            {
                tab.put(a, "key");
            }
        }

        return tab.keys();
    }

    public boolean engineContainsAlias(
        String  alias) 
    {
        return (certs.get(alias) != null || keys.get(alias) != null);
    }

    /**
     * this is not quite complete - we should follow up on the chain, a bit
     * tricky if a certificate appears in more than one chain...
     */
    public void engineDeleteEntry(
        String  alias) 
        throws KeyStoreException
    {
        Key k = (Key)keys.remove(alias);

        Certificate c = (Certificate)certs.remove(alias);

        if (c != null)
        {
            chainCerts.remove(new CertId(c.getPublicKey()));
        }

        if (k != null)
        {
            String  id = (String)localIds.remove(alias);
            if (id != null)
            {
                c = (Certificate)keyCerts.remove(id);
            }
            if (c != null)
            {
                chainCerts.remove(new CertId(c.getPublicKey()));
            }
        }

        if (c == null && k == null)
        {
            throw new KeyStoreException("no such entry as " + alias);
        }
    }

    /**
     * simply return the cert for the private key
     */
    public Certificate engineGetCertificate(
        String alias) 
    {
        if (alias == null)
        {
            throw new IllegalArgumentException("null alias passed to getCertificate.");
        }
        
        Certificate c = (Certificate)certs.get(alias);

        //
        // look up the key table - and try the local key id
        //
        if (c == null)
        {
            String  id = (String)localIds.get(alias);
            if (id != null)
            {
                c = (Certificate)keyCerts.get(id);
            }
            else
            {
                c = (Certificate)keyCerts.get(alias);
            }
        }

        return c;
    }

    public String engineGetCertificateAlias(
        Certificate cert) 
    {
        Enumeration c = certs.elements();
        Enumeration k = certs.keys();

        while (c.hasMoreElements())
        {
            Certificate tc = (Certificate)c.nextElement();
            String      ta = (String)k.nextElement();

            if (tc.equals(cert))
            {
                return ta;
            }
        }

        c = keyCerts.elements();
        k = keyCerts.keys();

        while (c.hasMoreElements())
        {
            Certificate tc = (Certificate)c.nextElement();
            String      ta = (String)k.nextElement();

            if (tc.equals(cert))
            {
                return ta;
            }
        }
        
        return null;
    }
    
    public Certificate[] engineGetCertificateChain(
        String alias) 
    {
        if (alias == null)
        {
            throw new IllegalArgumentException("null alias passed to getCertificateChain.");
        }
        
        if (!engineIsKeyEntry(alias))
        {
            return null;
        }
        
        Certificate c = engineGetCertificate(alias);

        if (c != null)
        {
            Vector  cs = new Vector();

            while (c != null)
            {
                X509Certificate     x509c = (X509Certificate)c;
                Certificate         nextC = null;

                byte[]  bytes = x509c.getExtensionValue(X509Extensions.AuthorityKeyIdentifier.getId());
                if (bytes != null)
                {
                    try
                    {
                        ASN1InputStream         aIn = new ASN1InputStream(bytes);

                        byte[] authBytes = ((ASN1OctetString)aIn.readObject()).getOctets();
                        aIn = new ASN1InputStream(authBytes);

                        AuthorityKeyIdentifier id = new AuthorityKeyIdentifier((ASN1Sequence)aIn.readObject());
                        if (id.getKeyIdentifier() != null)
                        {
                            nextC = (Certificate)chainCerts.get(new CertId(id.getKeyIdentifier()));
                        }
                        
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e.toString());
                    }
                }

                if (nextC == null)
                {
                    //
                    // no authority key id, try the Issuer DN
                    //
                    Principal  i = x509c.getIssuerDN();
                    Principal  s = x509c.getSubjectDN();

                    if (!i.equals(s))
                    {
                        Enumeration e = chainCerts.keys();

                        while (e.hasMoreElements())
                        {
                            X509Certificate crt = (X509Certificate)chainCerts.get(e.nextElement());
                            Principal  sub = crt.getSubjectDN();
                            if (sub.equals(i))
                            {
                                try
                                {
                                    x509c.verify(crt.getPublicKey());
                                    nextC = crt;
                                    break;
                                }
                                catch (Exception ex)
                                {
                                    // continue
                                }
                            }
                        }
                    }
                }

                cs.addElement(c);
                if (nextC != c)     // self signed - end of the chain
                {
                    c = nextC;
                }
                else
                {
                    c = null;
                }
            }

            Certificate[]   certChain = new Certificate[cs.size()];

            for (int i = 0; i != certChain.length; i++)
            {
                certChain[i] = (Certificate)cs.elementAt(i);
            }

            return certChain;
        }

        return null;
    }
    
    public Date engineGetCreationDate(String alias) 
    {
        return new Date();
    }

    public Key engineGetKey(
        String alias,
        char[] password) 
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        if (alias == null)
        {
            throw new IllegalArgumentException("null alias passed to getKey.");
        }
        
        return (Key)keys.get(alias);
    }

    public boolean engineIsCertificateEntry(
        String alias) 
    {
        return (certs.get(alias) != null && keys.get(alias) == null);
    }

    public boolean engineIsKeyEntry(
        String alias) 
    {
        return (keys.get(alias) != null);
    }

    public void engineSetCertificateEntry(
        String      alias,
        Certificate cert) 
        throws KeyStoreException
    {
        if (keys.get(alias) != null)
        {
            throw new KeyStoreException("There is a key entry with the name " + alias + ".");
        }

        certs.put(alias, cert);
        chainCerts.put(new CertId(cert.getPublicKey()), cert);
    }

    public void engineSetKeyEntry(
        String alias,
        byte[] key,
        Certificate[] chain) 
        throws KeyStoreException
    {
        throw new RuntimeException("operation not supported");
    }

    public void engineSetKeyEntry(
        String          alias,
        Key             key,
        char[]          password,
        Certificate[]   chain) 
        throws KeyStoreException
    {
        if ((key instanceof PrivateKey) && (chain == null))
        {
            throw new KeyStoreException("no certificate chain for private key");
        }

        if (keys.get(alias) != null)
        {
            engineDeleteEntry(alias);
        }

        keys.put(alias, key);
        certs.put(alias, chain[0]);

        for (int i = 0; i != chain.length; i++)
        {
            chainCerts.put(new CertId(chain[i].getPublicKey()), chain[i]);
        }
    }

    public int engineSize() 
    {
        Hashtable  tab = new Hashtable();

        Enumeration e = certs.keys();
        while (e.hasMoreElements())
        {
            tab.put(e.nextElement(), "cert");
        }

        e = keys.keys();
        while (e.hasMoreElements())
        {
            String  a = (String)e.nextElement();
            if (tab.get(a) == null)
            {
                tab.put(a, "key");
            }
        }

        return tab.size();
    }

    protected PrivateKey unwrapKey(
        AlgorithmIdentifier   algId,
        byte[]                data,
        char[]                password,
        boolean               wrongPKCS12Zero)
        throws IOException
    {
        String              algorithm = algId.getObjectId().getId();
        PKCS12PBEParams     pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());

        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        PrivateKey          out;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, bcProvider);
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            SecretKey           k = keyFact.generateSecret(pbeSpec);
            
            ((JCEPBEKey)k).setTryWrongPKCS12Zero(wrongPKCS12Zero);

            Cipher cipher = Cipher.getInstance(algorithm, bcProvider);

            cipher.init(Cipher.UNWRAP_MODE, k, defParams);

            // we pass "" as the key algorithm type as it is unknown at this point
            out = (PrivateKey)cipher.unwrap(data, "", Cipher.PRIVATE_KEY);
        }
        catch (Exception e)
        {
            throw new IOException("exception unwrapping private key - " + e.toString());
        }

        return out;
    }

    protected byte[] wrapKey(
        String                  algorithm,
        Key                     key,
        PKCS12PBEParams         pbeParams,
        char[]                  password)
        throws IOException
    {
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              out;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, bcProvider);
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm, bcProvider);

            cipher.init(Cipher.WRAP_MODE, keyFact.generateSecret(pbeSpec), defParams);

            out = cipher.wrap(key);
        }
        catch (Exception e)
        {
            throw new IOException("exception encrypting data - " + e.toString());
        }

        return out;
    }

    protected byte[] cryptData(
        boolean               forEncryption,
        AlgorithmIdentifier   algId,
        char[]                password,
        boolean               wrongPKCS12Zero,
        byte[]                data)
        throws IOException
    {
        String          algorithm = algId.getObjectId().getId();
        PKCS12PBEParams pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());
        PBEKeySpec      pbeSpec = new PBEKeySpec(password);

        try
        {
            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(algorithm, bcProvider);
            PBEParameterSpec defParams = new PBEParameterSpec(
                pbeParams.getIV(),
                pbeParams.getIterations().intValue());
            JCEPBEKey        key = (JCEPBEKey) keyFact.generateSecret(pbeSpec);

            key.setTryWrongPKCS12Zero(wrongPKCS12Zero);

            Cipher cipher = Cipher.getInstance(algorithm, bcProvider);
            int mode = forEncryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            cipher.init(mode, key, defParams);
            return cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IOException("exception decrypting data - " + e.toString());
        }
    }

    public void engineLoad(
        InputStream stream,
        char[]      password) 
        throws IOException
    {
        if (stream == null)     // just initialising
        {
            return;
        }

        if (password == null)
        {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }

        BufferedInputStream             bufIn = new BufferedInputStream(stream);

        bufIn.mark(10);

        int head = bufIn.read();

        if (head != 0x30)
        {
            throw new IOException("stream does not represent a PKCS12 key store");
        }

        bufIn.reset();

        ASN1InputStream bIn = new ASN1InputStream(bufIn);
        ASN1Sequence    obj = (ASN1Sequence)bIn.readObject();
        Pfx             bag = new Pfx(obj);
        ContentInfo     info = bag.getAuthSafe();
        Vector          chain = new Vector();
        boolean         unmarkedKey = false;
        boolean         wrongPKCS12Zero = false;

        if (bag.getMacData() != null)           // check the mac code
        {
            MacData                     mData = bag.getMacData();
            DigestInfo                  dInfo = mData.getMac();
            AlgorithmIdentifier         algId = dInfo.getAlgorithmId();
            byte[]                      salt = mData.getSalt();
            int                         itCount = mData.getIterationCount().intValue();

            byte[]  data = ((ASN1OctetString)info.getContent()).getOctets();

            try
            {
                byte[] res = calculatePbeMac(algId.getObjectId(), salt, itCount, password, false, data);
                byte[] dig = dInfo.getDigest();

                if (!Arrays.constantTimeAreEqual(res, dig))
                {
                    if (password.length > 0)
                    {
                        throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                    }

                    // Try with incorrect zero length password
                    res = calculatePbeMac(algId.getObjectId(), salt, itCount, password, true, data);

                    if (!Arrays.constantTimeAreEqual(res, dig))
                    {
                        throw new IOException("PKCS12 key store mac invalid - wrong password or corrupted file.");
                    }

                    wrongPKCS12Zero = true;
                }
            }
            catch (IOException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new IOException("error constructing MAC: " + e.toString());
            }
        }

        keys = new IgnoresCaseHashtable();
        localIds = new Hashtable();

        if (info.getContentType().equals(data))
        {
            bIn = new ASN1InputStream(((ASN1OctetString)info.getContent()).getOctets());

            AuthenticatedSafe   authSafe = new AuthenticatedSafe((ASN1Sequence)bIn.readObject());
            ContentInfo[]       c = authSafe.getContentInfo();

            for (int i = 0; i != c.length; i++)
            {
                if (c[i].getContentType().equals(data))
                {
                    ASN1InputStream dIn = new ASN1InputStream(((ASN1OctetString)c[i].getContent()).getOctets());
                    ASN1Sequence    seq = (ASN1Sequence)dIn.readObject();

                    for (int j = 0; j != seq.size(); j++)
                    {
                        SafeBag b = new SafeBag((ASN1Sequence)seq.getObjectAt(j));
                        if (b.getBagId().equals(pkcs8ShroudedKeyBag))
                        {
                            old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo eIn = new old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo((ASN1Sequence)b.getBagValue());
                            PrivateKey              privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password, wrongPKCS12Zero);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                                   alias = null;
                            ASN1OctetString                   localId = null;

                            if (b.getBagAttributes() != null)
                            {
                                Enumeration e = b.getBagAttributes().getObjects();
                                while (e.hasMoreElements())
                                {
                                    ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                                    DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                    ASN1Set                 attrSet = (ASN1Set)sq.getObjectAt(1);
                                    DERObject               attr = null;
    
                                    if (attrSet.size() > 0)
                                    {
                                        attr = (DERObject)attrSet.getObjectAt(0);

                                        DEREncodable existing = bagAttr.getBagAttribute(aOid);
                                        if (existing != null)
                                        {
                                            // OK, but the value has to be the same
                                            if (!existing.getDERObject().equals(attr))
                                            {
                                                throw new IOException(
                                                    "attempt to add existing attribute with different value");
                                            }
                                        }
                                        else
                                        {
                                            bagAttr.setBagAttribute(aOid, attr);
                                        }
                                    }
    
                                    if (aOid.equals(pkcs_9_at_friendlyName))
                                    {
                                        alias = ((DERBMPString)attr).getString();
                                        keys.put(alias, privKey);
                                    }
                                    else if (aOid.equals(pkcs_9_at_localKeyId))
                                    {
                                        localId = (ASN1OctetString)attr;
                                    }
                                }
                            }
                        
                            if (localId != null)
                            {
                                String name = new String(Hex.encode(localId.getOctets()));
    
                                if (alias == null)
                                {
                                    keys.put(name, privKey);
                                }
                                else
                                {
                                    localIds.put(alias, name);
                                }
                             }
                             else
                             {
                                 unmarkedKey = true;
                                 keys.put("unmarked", privKey);
                             }
                        }
                        else if (b.getBagId().equals(certBag))
                        {
                            chain.addElement(b);
                        }
                        else
                        {
                            System.out.println("extra in data " + b.getBagId());
                            System.out.println(ASN1Dump.dumpAsString(b));
                        }
                    }
                }
                else if (c[i].getContentType().equals(encryptedData))
                {
                    EncryptedData d = new EncryptedData((ASN1Sequence)c[i].getContent());
                    byte[] octets = cryptData(false, d.getEncryptionAlgorithm(),
                        password, wrongPKCS12Zero, d.getContent().getOctets());
                    ASN1Sequence seq = (ASN1Sequence) ASN1Object.fromByteArray(octets);

                    for (int j = 0; j != seq.size(); j++)
                    {
                        SafeBag b = new SafeBag((ASN1Sequence)seq.getObjectAt(j));
                        
                        if (b.getBagId().equals(certBag))
                        {
                            chain.addElement(b);
                        }
                        else if (b.getBagId().equals(pkcs8ShroudedKeyBag))
                        {
                            old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo eIn = new old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo((ASN1Sequence)b.getBagValue());
                            PrivateKey              privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password, wrongPKCS12Zero);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                      alias = null;
                            ASN1OctetString              localId = null;

                            Enumeration e = b.getBagAttributes().getObjects();
                            while (e.hasMoreElements())
                            {
                                ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                                DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                ASN1Set                 attrSet= (ASN1Set)sq.getObjectAt(1);
                                DERObject               attr = null;

                                if (attrSet.size() > 0)
                                {
                                    attr = (DERObject)attrSet.getObjectAt(0);

                                    DEREncodable existing = bagAttr.getBagAttribute(aOid);
                                    if (existing != null)
                                    {
                                        // OK, but the value has to be the same
                                        if (!existing.getDERObject().equals(attr))
                                        {
                                            throw new IOException(
                                                "attempt to add existing attribute with different value");
                                        }
                                    }
                                    else
                                    {
                                        bagAttr.setBagAttribute(aOid, attr);
                                    }
                                }

                                if (aOid.equals(pkcs_9_at_friendlyName))
                                {
                                    alias = ((DERBMPString)attr).getString();
                                    keys.put(alias, privKey);
                                }
                                else if (aOid.equals(pkcs_9_at_localKeyId))
                                {
                                    localId = (ASN1OctetString)attr;
                                }
                            }

                            String name = new String(Hex.encode(localId.getOctets()));

                            if (alias == null)
                            {
                                keys.put(name, privKey);
                            }
                            else
                            {
                                localIds.put(alias, name);
                            }
                        }
                        else if (b.getBagId().equals(keyBag))
                        {
                            old.org.bouncycastle.asn1.pkcs.PrivateKeyInfo pIn = new old.org.bouncycastle.asn1.pkcs.PrivateKeyInfo((ASN1Sequence)b.getBagValue());
                            PrivateKey              privKey = JDKKeyFactory.createPrivateKeyFromPrivateKeyInfo(pIn);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                      alias = null;
                            ASN1OctetString             localId = null;

                            Enumeration e = b.getBagAttributes().getObjects();
                            while (e.hasMoreElements())
                            {
                                ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                                DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                ASN1Set                 attrSet = (ASN1Set)sq.getObjectAt(1);
                                DERObject   attr = null;

                                if (attrSet.size() > 0)
                                {
                                    attr = (DERObject)attrSet.getObjectAt(0);

                                    DEREncodable existing = bagAttr.getBagAttribute(aOid);
                                    if (existing != null)
                                    {
                                        // OK, but the value has to be the same
                                        if (!existing.getDERObject().equals(attr))
                                        {
                                            throw new IOException(
                                                "attempt to add existing attribute with different value");
                                        }
                                    }
                                    else
                                    {
                                        bagAttr.setBagAttribute(aOid, attr);
                                    }
                                }

                                if (aOid.equals(pkcs_9_at_friendlyName))
                                {
                                    alias = ((DERBMPString)attr).getString();
                                    keys.put(alias, privKey);
                                }
                                else if (aOid.equals(pkcs_9_at_localKeyId))
                                {
                                    localId = (ASN1OctetString)attr;
                                }
                            }

                            String name = new String(Hex.encode(localId.getOctets()));

                            if (alias == null)
                            {
                                keys.put(name, privKey);
                            }
                            else
                            {
                                localIds.put(alias, name);
                            }
                        }
                        else
                        {
                            System.out.println("extra in encryptedData " + b.getBagId());
                            System.out.println(ASN1Dump.dumpAsString(b));
                        }
                    }
                }
                else
                {
                    System.out.println("extra " + c[i].getContentType().getId());
                    System.out.println("extra " + ASN1Dump.dumpAsString(c[i].getContent()));
                }
            }
        }

        certs = new IgnoresCaseHashtable();
        chainCerts = new Hashtable();
        keyCerts = new Hashtable();

        for (int i = 0; i != chain.size(); i++)
        {
            SafeBag     b = (SafeBag)chain.elementAt(i);
            CertBag     cb = new CertBag((ASN1Sequence)b.getBagValue());

            if (!cb.getCertId().equals(x509Certificate))
            {
                throw new RuntimeException("Unsupported certificate type: " + cb.getCertId());
            }

            Certificate cert;

            try
            {
                ByteArrayInputStream  cIn = new ByteArrayInputStream(
                                ((ASN1OctetString)cb.getCertValue()).getOctets());
                cert = certFact.generateCertificate(cIn);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.toString());
            }

            //
            // set the attributes
            //
            ASN1OctetString localId = null;
            String          alias = null;

            if (b.getBagAttributes() != null)
            {
                Enumeration e = b.getBagAttributes().getObjects();
                while (e.hasMoreElements())
                {
                    ASN1Sequence  sq = (ASN1Sequence)e.nextElement();
                    DERObjectIdentifier     oid = (DERObjectIdentifier)sq.getObjectAt(0);
                    DERObject               attr = (DERObject)((ASN1Set)sq.getObjectAt(1)).getObjectAt(0);
                    PKCS12BagAttributeCarrier   bagAttr = null;

                    if (cert instanceof PKCS12BagAttributeCarrier)
                    {
                        bagAttr = (PKCS12BagAttributeCarrier)cert;

                        DEREncodable existing = bagAttr.getBagAttribute(oid);
                        if (existing != null)
                        {
                            // OK, but the value has to be the same
                            if (!existing.getDERObject().equals(attr))
                            {
                                throw new IOException(
                                    "attempt to add existing attribute with different value");
                            }
                        }
                        else
                        {
                            bagAttr.setBagAttribute(oid, attr);
                        }
                    }

                    if (oid.equals(pkcs_9_at_friendlyName))
                    {
                        alias = ((DERBMPString)attr).getString();
                    }
                    else if (oid.equals(pkcs_9_at_localKeyId))
                    {
                        localId = (ASN1OctetString)attr;
                    }
                }
            }

            chainCerts.put(new CertId(cert.getPublicKey()), cert);

            if (unmarkedKey)
            {
                if (keyCerts.isEmpty())
                {
                    String    name = new String(Hex.encode(createSubjectKeyId(cert.getPublicKey()).getKeyIdentifier()));
                    
                    keyCerts.put(name, cert);
                    keys.put(name, keys.remove("unmarked"));
                }
            }
            else
            {
                //
                // the local key id needs to override the friendly name
                //
                if (localId != null)
                {
                    String name = new String(Hex.encode(localId.getOctets()));

                    keyCerts.put(name, cert);
                }
                if (alias != null)
                {
                    certs.put(alias, cert);
                }
            }
        }
    }

    public void engineStore(LoadStoreParameter param) throws IOException,
            NoSuchAlgorithmException, CertificateException
    {
        if (param == null)
        {
            throw new IllegalArgumentException("'param' arg cannot be null");
        }

        if (!(param instanceof JDKPKCS12StoreParameter))
        {
            throw new IllegalArgumentException(
                "No support for 'param' of type " + param.getClass().getName());
        }

        JDKPKCS12StoreParameter bcParam = (JDKPKCS12StoreParameter)param;

        char[] password;
        ProtectionParameter protParam = param.getProtectionParameter();
        if (protParam == null)
        {
            password = null;
        }
        else if (protParam instanceof KeyStore.PasswordProtection)
        {
            password = ((KeyStore.PasswordProtection)protParam).getPassword();
        }
        else
        {
            throw new IllegalArgumentException(
                "No support for protection parameter of type " + protParam.getClass().getName());
        }

        doStore(bcParam.getOutputStream(), password, bcParam.isUseDEREncoding());
    }

    public void engineStore(OutputStream stream, char[] password) 
        throws IOException
    {
        doStore(stream, password, false);
    }

    private void doStore(OutputStream stream, char[] password, boolean useDEREncoding) 
        throws IOException
    {
        if (password == null)
        {
            throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
        }

        //
        // handle the key
        //
        ASN1EncodableVector  keyS = new ASN1EncodableVector();


        Enumeration ks = keys.keys();

        while (ks.hasMoreElements())
        {
            byte[]                  kSalt = new byte[SALT_SIZE];

            random.nextBytes(kSalt);

            String                  name = (String)ks.nextElement();
            PrivateKey              privKey = (PrivateKey)keys.get(name);
            PKCS12PBEParams         kParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
            byte[]                  kBytes = wrapKey(keyAlgorithm.getId(), privKey, kParams, password);
            AlgorithmIdentifier     kAlgId = new AlgorithmIdentifier(keyAlgorithm, kParams.getDERObject());
            old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo kInfo = new old.org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo(kAlgId, kBytes);
            boolean                 attrSet = false;
            ASN1EncodableVector     kName = new ASN1EncodableVector();

            if (privKey instanceof PKCS12BagAttributeCarrier)
            {
                PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)privKey;
                //
                // make sure we are using the local alias on store
                //
                DERBMPString    nm = (DERBMPString)bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                if (nm == null || !nm.getString().equals(name))
                {
                    bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                }

                //
                // make sure we have a local key-id
                //
                if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) == null)
                {
                    Certificate             ct = engineGetCertificate(name);

                    bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(ct.getPublicKey()));
                }

                Enumeration e = bagAttrs.getBagAttributeKeys();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    ASN1EncodableVector  kSeq = new ASN1EncodableVector();

                    kSeq.add(oid);
                    kSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));

                    attrSet = true;

                    kName.add(new DERSequence(kSeq));
                }
            }

            if (!attrSet)
            {
                //
                // set a default friendly name (from the key id) and local id
                //
                ASN1EncodableVector     kSeq = new ASN1EncodableVector();
                Certificate             ct = engineGetCertificate(name);

                kSeq.add(pkcs_9_at_localKeyId);
                kSeq.add(new DERSet(createSubjectKeyId(ct.getPublicKey())));

                kName.add(new DERSequence(kSeq));

                kSeq = new ASN1EncodableVector();

                kSeq.add(pkcs_9_at_friendlyName);
                kSeq.add(new DERSet(new DERBMPString(name)));

                kName.add(new DERSequence(kSeq));
            }

            SafeBag                 kBag = new SafeBag(pkcs8ShroudedKeyBag, kInfo.getDERObject(), new DERSet(kName));
            keyS.add(kBag);
        }

        byte[]                    keySEncoded = new DERSequence(keyS).getDEREncoded();
        BERConstructedOctetString keyString = new BERConstructedOctetString(keySEncoded);

        //
        // certificate processing
        //
        byte[]                  cSalt = new byte[SALT_SIZE];

        random.nextBytes(cSalt);

        ASN1EncodableVector  certSeq = new ASN1EncodableVector();
        PKCS12PBEParams         cParams = new PKCS12PBEParams(cSalt, MIN_ITERATIONS);
        AlgorithmIdentifier     cAlgId = new AlgorithmIdentifier(certAlgorithm, cParams.getDERObject());
        Hashtable               doneCerts = new Hashtable();

        Enumeration cs = keys.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                String              name = (String)cs.nextElement();
                Certificate         cert = engineGetCertificate(name);
                boolean             cAttrSet = false;
                CertBag             cBag = new CertBag(
                                        x509Certificate,
                                        new DEROctetString(cert.getEncoded()));
                ASN1EncodableVector fName = new ASN1EncodableVector();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    //
                    // make sure we are using the local alias on store
                    //
                    DERBMPString    nm = (DERBMPString)bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                    if (nm == null || !nm.getString().equals(name))
                    {
                        bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(name));
                    }

                    //
                    // make sure we have a local key-id
                    //
                    if (bagAttrs.getBagAttribute(pkcs_9_at_localKeyId) == null)
                    {
                        bagAttrs.setBagAttribute(pkcs_9_at_localKeyId, createSubjectKeyId(cert.getPublicKey()));
                    }

                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        ASN1EncodableVector fSeq = new ASN1EncodableVector();

                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));

                        cAttrSet = true;
                    }
                }

                if (!cAttrSet)
                {
                    ASN1EncodableVector  fSeq = new ASN1EncodableVector();

                    fSeq.add(pkcs_9_at_localKeyId);
                    fSeq.add(new DERSet(createSubjectKeyId(cert.getPublicKey())));
                    fName.add(new DERSequence(fSeq));

                    fSeq = new ASN1EncodableVector();

                    fSeq.add(pkcs_9_at_friendlyName);
                    fSeq.add(new DERSet(new DERBMPString(name)));

                    fName.add(new DERSequence(fSeq));
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), new DERSet(fName));

                certSeq.add(sBag);

                doneCerts.put(cert, cert);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        cs = certs.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                String              certId = (String)cs.nextElement();
                Certificate         cert = (Certificate)certs.get(certId);
                boolean             cAttrSet = false;

                if (keys.get(certId) != null)
                {
                    continue;
                }

                CertBag             cBag = new CertBag(
                                        x509Certificate,
                                        new DEROctetString(cert.getEncoded()));
                ASN1EncodableVector fName = new ASN1EncodableVector();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    //
                    // make sure we are using the local alias on store
                    //
                    DERBMPString    nm = (DERBMPString)bagAttrs.getBagAttribute(pkcs_9_at_friendlyName);
                    if (nm == null || !nm.getString().equals(certId))
                    {
                        bagAttrs.setBagAttribute(pkcs_9_at_friendlyName, new DERBMPString(certId));
                    }

                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();

                        // a certificate not immediately linked to a key doesn't require
                        // a localKeyID and will confuse some PKCS12 implementations.
                        //
                        // If we find one, we'll prune it out.
                        if (oid.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId))
                        {
                            continue;
                        }

                        ASN1EncodableVector fSeq = new ASN1EncodableVector();

                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));

                        cAttrSet = true;
                    }
                }

                if (!cAttrSet)
                {
                    ASN1EncodableVector  fSeq = new ASN1EncodableVector();

                    fSeq.add(pkcs_9_at_friendlyName);
                    fSeq.add(new DERSet(new DERBMPString(certId)));

                    fName.add(new DERSequence(fSeq));
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), new DERSet(fName));

                certSeq.add(sBag);

                doneCerts.put(cert, cert);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        cs = chainCerts.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                CertId              certId = (CertId)cs.nextElement();
                Certificate         cert = (Certificate)chainCerts.get(certId);

                if (doneCerts.get(cert) != null)
                {
                    continue;
                }

                CertBag             cBag = new CertBag(
                                        x509Certificate,
                                        new DEROctetString(cert.getEncoded()));
                ASN1EncodableVector fName = new ASN1EncodableVector();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();

                        // a certificate not immediately linked to a key doesn't require
                        // a localKeyID and will confuse some PKCS12 implementations.
                        //
                        // If we find one, we'll prune it out.
                        if (oid.equals(PKCSObjectIdentifiers.pkcs_9_at_localKeyId))
                        {
                            continue;
                        }

                        ASN1EncodableVector fSeq = new ASN1EncodableVector();

                        fSeq.add(oid);
                        fSeq.add(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.add(new DERSequence(fSeq));
                    }
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), new DERSet(fName));

                certSeq.add(sBag);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        byte[]          certSeqEncoded = new DERSequence(certSeq).getDEREncoded();
        byte[]          certBytes = cryptData(true, cAlgId, password, false, certSeqEncoded);
        EncryptedData   cInfo = new EncryptedData(data, cAlgId, new BERConstructedOctetString(certBytes));

        ContentInfo[] info = new ContentInfo[]
        {
            new ContentInfo(data, keyString),
            new ContentInfo(encryptedData, cInfo.getDERObject())
        };

        AuthenticatedSafe   auth = new AuthenticatedSafe(info);

        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream asn1Out;
        if (useDEREncoding)
        {
            asn1Out = new DEROutputStream(bOut);
        }
        else
        {
            asn1Out = new BEROutputStream(bOut);
        }

        asn1Out.writeObject(auth);

        byte[]              pkg = bOut.toByteArray();

        ContentInfo         mainInfo = new ContentInfo(data, new BERConstructedOctetString(pkg));

        //
        // create the mac
        //
        byte[]                      mSalt = new byte[20];
        int                         itCount = MIN_ITERATIONS;

        random.nextBytes(mSalt);
    
        byte[]  data = ((ASN1OctetString)mainInfo.getContent()).getOctets();

        MacData                 mData;

        try
        {
            byte[] res = calculatePbeMac(id_SHA1, mSalt, itCount, password, false, data);

            AlgorithmIdentifier     algId = new AlgorithmIdentifier(id_SHA1, new DERNull());
            DigestInfo              dInfo = new DigestInfo(algId, res);

            mData = new MacData(dInfo, mSalt, itCount);
        }
        catch (Exception e)
        {
            throw new IOException("error constructing MAC: " + e.toString());
        }
        
        //
        // output the Pfx
        //
        Pfx                 pfx = new Pfx(mainInfo, mData);

        if (useDEREncoding)
        {
            asn1Out = new DEROutputStream(stream);
        }
        else
        {
            asn1Out = new BEROutputStream(stream);
        }

        asn1Out.writeObject(pfx);
    }

    private static byte[] calculatePbeMac(
        DERObjectIdentifier oid,
        byte[]              salt,
        int                 itCount,
        char[]              password,
        boolean             wrongPkcs12Zero,
        byte[]              data)
        throws Exception
    {
        SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(oid.getId(), bcProvider);
        PBEParameterSpec    defParams = new PBEParameterSpec(salt, itCount);
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        JCEPBEKey           key = (JCEPBEKey) keyFact.generateSecret(pbeSpec);
        key.setTryWrongPKCS12Zero(wrongPkcs12Zero);

        Mac mac = Mac.getInstance(oid.getId(), bcProvider);
        mac.init(key, defParams);
        mac.update(data);
        return mac.doFinal();
    }
    
    public static class BCPKCS12KeyStore
        extends JDKPKCS12KeyStore
    {
        public BCPKCS12KeyStore()
        {
            super(bcProvider, pbeWithSHAAnd3_KeyTripleDES_CBC, pbewithSHAAnd40BitRC2_CBC);
        }
    }

    public static class BCPKCS12KeyStore3DES
        extends JDKPKCS12KeyStore
    {
        public BCPKCS12KeyStore3DES()
        {
            super(bcProvider, pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC);
        }
    }

    public static class DefPKCS12KeyStore
        extends JDKPKCS12KeyStore
    {
        public DefPKCS12KeyStore()
        {
            super(null, pbeWithSHAAnd3_KeyTripleDES_CBC, pbewithSHAAnd40BitRC2_CBC);
        }
    }

    public static class DefPKCS12KeyStore3DES
        extends JDKPKCS12KeyStore
    {
        public DefPKCS12KeyStore3DES()
        {
            super(null, pbeWithSHAAnd3_KeyTripleDES_CBC, pbeWithSHAAnd3_KeyTripleDES_CBC);
        }
    }

    private static class IgnoresCaseHashtable
    {
        private Hashtable orig = new Hashtable();
        private Hashtable keys = new Hashtable();

        public void put(String key, Object value)
        {
            String lower = Strings.toLowerCase(key);
            String k = (String)keys.get(lower);
            if (k != null)
            {
                orig.remove(k);
            }

            keys.put(lower, key);
            orig.put(key, value);
        }

        public Enumeration keys()
        {
            return orig.keys();
        }

        public Object remove(String alias)
        {
            String k = (String)keys.remove(Strings.toLowerCase(alias));
            if (k == null)
            {
                return null;
            }

            return orig.remove(k);
        }

        public Object get(String alias)
        {
            String k = (String)keys.get(Strings.toLowerCase(alias));
            if (k == null)
            {
                return null;
            }
            
            return orig.get(k);
        }

        public Enumeration elements()
        {
            return orig.elements();
        }
    }
}
