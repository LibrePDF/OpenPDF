package old.org.bouncycastle.jce.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.crypto.CipherParameters;
import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.PBEParametersGenerator;
import old.org.bouncycastle.crypto.digests.SHA1Digest;
import old.org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import old.org.bouncycastle.crypto.io.DigestInputStream;
import old.org.bouncycastle.crypto.io.DigestOutputStream;
import old.org.bouncycastle.crypto.io.MacInputStream;
import old.org.bouncycastle.crypto.io.MacOutputStream;
import old.org.bouncycastle.crypto.macs.HMac;
import old.org.bouncycastle.jce.interfaces.BCKeyStore;
import old.org.bouncycastle.util.Arrays;
import old.org.bouncycastle.util.io.Streams;

public class JDKKeyStore
    extends KeyStoreSpi
    implements BCKeyStore
{
    private static final int    STORE_VERSION = 1;

    private static final int    STORE_SALT_SIZE = 20;
    private static final String STORE_CIPHER = "PBEWithSHAAndTwofish-CBC";

    private static final int    KEY_SALT_SIZE = 20;
    private static final int    MIN_ITERATIONS = 1024;

    private static final String KEY_CIPHER = "PBEWithSHAAnd3-KeyTripleDES-CBC";

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

    protected Hashtable       table = new Hashtable();

    protected SecureRandom    random = new SecureRandom();

    public JDKKeyStore()
    {
    }

    private class StoreEntry
    {
        int             type;
        String          alias;
        Object          obj;
        Certificate[]   certChain;
        Date            date = new Date();

        StoreEntry(
            String       alias,
            Certificate  obj)
        {
            this.type = CERTIFICATE;
            this.alias = alias;
            this.obj = obj;
            this.certChain = null;
        }

        StoreEntry(
            String          alias,
            byte[]          obj,
            Certificate[]   certChain)
        {
            this.type = SECRET;
            this.alias = alias;
            this.obj = obj;
            this.certChain = certChain;
        }

        StoreEntry(
            String          alias,
            Key             key,
            char[]          password,
            Certificate[]   certChain)
            throws Exception
        {
            this.type = SEALED;
            this.alias = alias;
            this.certChain = certChain;

            byte[] salt = new byte[KEY_SALT_SIZE];

            random.setSeed(System.currentTimeMillis());
            random.nextBytes(salt);

            int iterationCount = MIN_ITERATIONS + (random.nextInt() & 0x3ff);


            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DataOutputStream        dOut = new DataOutputStream(bOut);

            dOut.writeInt(salt.length);
            dOut.write(salt);
            dOut.writeInt(iterationCount);

            Cipher              cipher = makePBECipher(KEY_CIPHER, Cipher.ENCRYPT_MODE, password, salt, iterationCount);
            CipherOutputStream  cOut = new CipherOutputStream(dOut, cipher);

            dOut = new DataOutputStream(cOut);

            encodeKey(key, dOut);

            dOut.close();

            obj = bOut.toByteArray();
        }

        StoreEntry(
            String          alias,
            Date            date,
            int             type,
            Object          obj)
        {
            this.alias = alias;
            this.date = date;
            this.type = type;
            this.obj = obj;
        }

        StoreEntry(
            String          alias,
            Date            date,
            int             type,
            Object          obj,
            Certificate[]   certChain)
        {
            this.alias = alias;
            this.date = date;
            this.type = type;
            this.obj = obj;
            this.certChain = certChain;
        }

        int getType()
        {
            return type;
        }

        String getAlias()
        {
            return alias;
        }

        Object getObject()
        {
            return obj;
        }

        Object getObject(
            char[]  password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException
        {
            if (password == null || password.length == 0)
            {
                if (obj instanceof Key)
                {
                    return obj;
                }
            }

            if (type == SEALED)
            {
                ByteArrayInputStream    bIn = new ByteArrayInputStream((byte[])obj);
                DataInputStream         dIn = new DataInputStream(bIn);
            
                try
                {
                    byte[]      salt = new byte[dIn.readInt()];

                    dIn.readFully(salt);

                    int     iterationCount = dIn.readInt();
                
                    Cipher      cipher = makePBECipher(KEY_CIPHER, Cipher.DECRYPT_MODE, password, salt, iterationCount);

                    CipherInputStream cIn = new CipherInputStream(dIn, cipher);

                    try
                    {
                        return decodeKey(new DataInputStream(cIn));
                    }
                    catch (Exception x)
                    {
                        bIn = new ByteArrayInputStream((byte[])obj);
                        dIn = new DataInputStream(bIn);
            
                        salt = new byte[dIn.readInt()];

                        dIn.readFully(salt);

                        iterationCount = dIn.readInt();

                        cipher = makePBECipher("Broken" + KEY_CIPHER, Cipher.DECRYPT_MODE, password, salt, iterationCount);

                        cIn = new CipherInputStream(dIn, cipher);

                        Key k = null;

                        try
                        {
                            k = decodeKey(new DataInputStream(cIn));
                        }
                        catch (Exception y)
                        {
                            bIn = new ByteArrayInputStream((byte[])obj);
                            dIn = new DataInputStream(bIn);
                
                            salt = new byte[dIn.readInt()];

                            dIn.readFully(salt);

                            iterationCount = dIn.readInt();

                            cipher = makePBECipher("Old" + KEY_CIPHER, Cipher.DECRYPT_MODE, password, salt, iterationCount);

                            cIn = new CipherInputStream(dIn, cipher);

                            k = decodeKey(new DataInputStream(cIn));
                        }

                        //
                        // reencrypt key with correct cipher.
                        //
                        if (k != null)
                        {
                            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
                            DataOutputStream        dOut = new DataOutputStream(bOut);

                            dOut.writeInt(salt.length);
                            dOut.write(salt);
                            dOut.writeInt(iterationCount);

                            Cipher              out = makePBECipher(KEY_CIPHER, Cipher.ENCRYPT_MODE, password, salt, iterationCount);
                            CipherOutputStream  cOut = new CipherOutputStream(dOut, out);

                            dOut = new DataOutputStream(cOut);

                            encodeKey(k, dOut);

                            dOut.close();

                            obj = bOut.toByteArray();

                            return k;
                        }
                        else
                        {
                            throw new UnrecoverableKeyException("no match");
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new UnrecoverableKeyException("no match");
                }
            }
            else
            {
                throw new RuntimeException("forget something!");
                // TODO
                // if we get to here key was saved as byte data, which
                // according to the docs means it must be a private key
                // in EncryptedPrivateKeyInfo (PKCS8 format), later...
                //
            }
        }

        Certificate[] getCertificateChain()
        {
            return certChain;
        }

        Date getDate()
        {
            return date;
        }
    }

    private void encodeCertificate(
        Certificate         cert,
        DataOutputStream    dOut)
        throws IOException
    {
        try
        {
            byte[]      cEnc = cert.getEncoded();

            dOut.writeUTF(cert.getType());
            dOut.writeInt(cEnc.length);
            dOut.write(cEnc);
        }
        catch (CertificateEncodingException ex)
        {
            throw new IOException(ex.toString());
        }
    }

    private Certificate decodeCertificate(
        DataInputStream   dIn)
        throws IOException
    {
        String      type = dIn.readUTF();
        byte[]      cEnc = new byte[dIn.readInt()];

        dIn.readFully(cEnc);

        try
        {
            CertificateFactory cFact = CertificateFactory.getInstance(type, BouncyCastleProvider.PROVIDER_NAME);
            ByteArrayInputStream bIn = new ByteArrayInputStream(cEnc);

            return cFact.generateCertificate(bIn);
        }
        catch (NoSuchProviderException ex)
        {
            throw new IOException(ex.toString());
        }
        catch (CertificateException ex)
        {
            throw new IOException(ex.toString());
        }
    }

    private void encodeKey(
        Key                 key,
        DataOutputStream    dOut)
        throws IOException
    {
        byte[]      enc = key.getEncoded();

        if (key instanceof PrivateKey)
        {
            dOut.write(KEY_PRIVATE);
        }
        else if (key instanceof PublicKey)
        {
            dOut.write(KEY_PUBLIC);
        }
        else
        {
            dOut.write(KEY_SECRET);
        }
    
        dOut.writeUTF(key.getFormat());
        dOut.writeUTF(key.getAlgorithm());
        dOut.writeInt(enc.length);
        dOut.write(enc);
    }

    private Key decodeKey(
        DataInputStream dIn)
        throws IOException
    {
        int         keyType = dIn.read();
        String      format = dIn.readUTF();
        String      algorithm = dIn.readUTF();
        byte[]      enc = new byte[dIn.readInt()];
        KeySpec     spec;

        dIn.readFully(enc);

        if (format.equals("PKCS#8") || format.equals("PKCS8"))
        {
            spec = new PKCS8EncodedKeySpec(enc);
        }
        else if (format.equals("X.509") || format.equals("X509"))
        {
            spec = new X509EncodedKeySpec(enc);
        }
        else if (format.equals("RAW"))
        {
            return new SecretKeySpec(enc, algorithm);
        }
        else
        {
            throw new IOException("Key format " + format + " not recognised!");
        }

        try
        {
            switch (keyType)
            {
            case KEY_PRIVATE:
                return KeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME).generatePrivate(spec);
            case KEY_PUBLIC:
                return KeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME).generatePublic(spec);
            case KEY_SECRET:
                return SecretKeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME).generateSecret(spec);
            default:
                throw new IOException("Key type " + keyType + " not recognised!");
            }
        }
        catch (Exception e)
        {
            throw new IOException("Exception creating key: " + e.toString());
        }
    }

    protected Cipher makePBECipher(
        String  algorithm,
        int     mode,
        char[]  password,
        byte[]  salt,
        int     iterationCount)
        throws IOException
    {
        try
        {
            PBEKeySpec          pbeSpec = new PBEKeySpec(password);
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);
            PBEParameterSpec    defParams = new PBEParameterSpec(salt, iterationCount);

            Cipher cipher = Cipher.getInstance(algorithm, BouncyCastleProvider.PROVIDER_NAME);

            cipher.init(mode, keyFact.generateSecret(pbeSpec), defParams);

            return cipher;
        }
        catch (Exception e)
        {
            throw new IOException("Error initialising store of key store: " + e);
        }
    }

    public void setRandom(
            SecureRandom    rand)
    {
        this.random = rand;
    }

    public Enumeration engineAliases() 
    {
        return table.keys();
    }

    public boolean engineContainsAlias(
        String  alias) 
    {
        return (table.get(alias) != null);
    }

    public void engineDeleteEntry(
        String  alias) 
        throws KeyStoreException
    {
        Object  entry = table.get(alias);

        if (entry == null)
        {
            throw new KeyStoreException("no such entry as " + alias);
        }

        table.remove(alias);
    }

    public Certificate engineGetCertificate(
        String alias) 
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry != null)
        {
            if (entry.getType() == CERTIFICATE)
            {
                return (Certificate)entry.getObject();
            }
            else
            {
                Certificate[]   chain = entry.getCertificateChain();

                if (chain != null)
                {
                    return chain[0];
                }
            }
        }

        return null;
    }

    public String engineGetCertificateAlias(
        Certificate cert) 
    {
        Enumeration e = table.elements();
        while (e.hasMoreElements())
        {
            StoreEntry  entry = (StoreEntry)e.nextElement();

            if (entry.getObject() instanceof Certificate)
            {
                Certificate c = (Certificate)entry.getObject();

                if (c.equals(cert))
                {
                    return entry.getAlias();
                }
            }
            else
            {
                Certificate[]   chain = entry.getCertificateChain();

                if (chain != null && chain[0].equals(cert))
                {
                    return entry.getAlias();
                }
            }
        }

        return null;
    }
    
    public Certificate[] engineGetCertificateChain(
        String alias) 
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry != null)
        {
            return entry.getCertificateChain();
        }

        return null;
    }
    
    public Date engineGetCreationDate(String alias) 
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry != null)
        {
            return entry.getDate();
        }

        return null;
    }

    public Key engineGetKey(
        String alias,
        char[] password) 
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry == null || entry.getType() == CERTIFICATE)
        {
            return null;
        }

        return (Key)entry.getObject(password);
    }

    public boolean engineIsCertificateEntry(
        String alias) 
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry != null && entry.getType() == CERTIFICATE)
        {
            return true;
        }
    
        return false;
    }

    public boolean engineIsKeyEntry(
        String alias) 
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry != null && entry.getType() != CERTIFICATE)
        {
            return true;
        }
    
        return false;
    }

    public void engineSetCertificateEntry(
        String      alias,
        Certificate cert) 
        throws KeyStoreException
    {
        StoreEntry  entry = (StoreEntry)table.get(alias);

        if (entry != null && entry.getType() != CERTIFICATE)
        {
            throw new KeyStoreException("key store already has a key entry with alias " + alias);
        }

        table.put(alias, new StoreEntry(alias, cert));
    }

    public void engineSetKeyEntry(
        String alias,
        byte[] key,
        Certificate[] chain) 
        throws KeyStoreException
    {
        table.put(alias, new StoreEntry(alias, key, chain));
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

        try
        {
            table.put(alias, new StoreEntry(alias, key, password, chain));
        }
        catch (Exception e)
        {
            throw new KeyStoreException(e.toString());
        }
    }

    public int engineSize() 
    {
        return table.size();
    }

    protected void loadStore(
        InputStream in)
        throws IOException
    {
        DataInputStream     dIn = new DataInputStream(in);
        int                 type = dIn.read();

        while (type > NULL)
        {
            String          alias = dIn.readUTF();
            Date            date = new Date(dIn.readLong());
            int             chainLength = dIn.readInt();
            Certificate[]   chain = null;

            if (chainLength != 0)
            {
                chain = new Certificate[chainLength];

                for (int i = 0; i != chainLength; i++)
                {
                    chain[i] = decodeCertificate(dIn);
                }
            }

            switch (type)
            {
            case CERTIFICATE:
                    Certificate     cert = decodeCertificate(dIn);

                    table.put(alias, new StoreEntry(alias, date, CERTIFICATE, cert));
                    break;
            case KEY:
                    Key     key = decodeKey(dIn);
                    table.put(alias, new StoreEntry(alias, date, KEY, key, chain));
                    break;
            case SECRET:
            case SEALED:
                    byte[]      b = new byte[dIn.readInt()];

                    dIn.readFully(b);
                    table.put(alias, new StoreEntry(alias, date, type, b, chain));
                    break;
            default:
                    throw new RuntimeException("Unknown object type in store.");
            }

            type = dIn.read();
        }
    }

    protected void saveStore(
        OutputStream    out)
        throws IOException
    {
        Enumeration         e = table.elements();
        DataOutputStream    dOut = new DataOutputStream(out);

        while (e.hasMoreElements())
        {
            StoreEntry  entry = (StoreEntry)e.nextElement();

            dOut.write(entry.getType());
            dOut.writeUTF(entry.getAlias());
            dOut.writeLong(entry.getDate().getTime());

            Certificate[]   chain = entry.getCertificateChain();
            if (chain == null)
            {
                dOut.writeInt(0);
            }
            else
            {
                dOut.writeInt(chain.length);
                for (int i = 0; i != chain.length; i++)
                {
                    encodeCertificate(chain[i], dOut);
                }
            }

            switch (entry.getType())
            {
            case CERTIFICATE:
                    encodeCertificate((Certificate)entry.getObject(), dOut);
                    break;
            case KEY:
                    encodeKey((Key)entry.getObject(), dOut);
                    break;
            case SEALED:
            case SECRET:
                    byte[]  b = (byte[])entry.getObject();

                    dOut.writeInt(b.length);
                    dOut.write(b);
                    break;
            default:
                    throw new RuntimeException("Unknown object type in store.");
            }
        }

        dOut.write(NULL);
    }

    public void engineLoad(
        InputStream stream,
        char[]      password) 
        throws IOException
    {
        table.clear();

        if (stream == null)     // just initialising
        {
            return;
        }

        DataInputStream     dIn = new DataInputStream(stream);
        int                 version = dIn.readInt();

        if (version != STORE_VERSION)
        {
            if (version != 0)
            {
                throw new IOException("Wrong version of key store.");
            }
        }

        byte[]      salt = new byte[dIn.readInt()];

        dIn.readFully(salt);

        int         iterationCount = dIn.readInt();

        //
        // we only do an integrity check if the password is provided.
        //
        HMac hMac = new HMac(new SHA1Digest());
        if (password != null && password.length != 0)
        {
            byte[] passKey = PBEParametersGenerator.PKCS12PasswordToBytes(password);

            PBEParametersGenerator pbeGen = new PKCS12ParametersGenerator(new SHA1Digest());
            pbeGen.init(passKey, salt, iterationCount);
            CipherParameters macParams = pbeGen.generateDerivedMacParameters(hMac.getMacSize());
            Arrays.fill(passKey, (byte)0);

            hMac.init(macParams);
            MacInputStream mIn = new MacInputStream(dIn, hMac);

            loadStore(mIn);

            // Finalise our mac calculation
            byte[] mac = new byte[hMac.getMacSize()];
            hMac.doFinal(mac, 0);

            // TODO Should this actually be reading the remainder of the stream?
            // Read the original mac from the stream
            byte[] oldMac = new byte[hMac.getMacSize()];
            dIn.readFully(oldMac);

            if (!Arrays.constantTimeAreEqual(mac, oldMac))
            {
                table.clear();
                throw new IOException("KeyStore integrity check failed.");
            }
        }
        else
        {
            loadStore(dIn);

            // TODO Should this actually be reading the remainder of the stream?
            // Parse the original mac from the stream too
            byte[] oldMac = new byte[hMac.getMacSize()];
            dIn.readFully(oldMac);
        }
    }


    public void engineStore(OutputStream stream, char[] password) 
        throws IOException
    {
        DataOutputStream    dOut = new DataOutputStream(stream);
        byte[]              salt = new byte[STORE_SALT_SIZE];
        int                 iterationCount = MIN_ITERATIONS + (random.nextInt() & 0x3ff);

        random.nextBytes(salt);

        dOut.writeInt(STORE_VERSION);
        dOut.writeInt(salt.length);
        dOut.write(salt);
        dOut.writeInt(iterationCount);

        HMac                    hMac = new HMac(new SHA1Digest());
        MacOutputStream         mOut = new MacOutputStream(dOut, hMac);
        PBEParametersGenerator  pbeGen = new PKCS12ParametersGenerator(new SHA1Digest());
        byte[]                  passKey = PBEParametersGenerator.PKCS12PasswordToBytes(password);

        pbeGen.init(passKey, salt, iterationCount);

        hMac.init(pbeGen.generateDerivedMacParameters(hMac.getMacSize()));

        for (int i = 0; i != passKey.length; i++)
        {
            passKey[i] = 0;
        }

        saveStore(mOut);

        byte[]  mac = new byte[hMac.getMacSize()];

        hMac.doFinal(mac, 0);

        dOut.write(mac);

        dOut.close();
    }

    /**
     * the BouncyCastle store. This wont work with the key tool as the
     * store is stored encrypteed on disk, so the password is mandatory,
     * however if you hard drive is in a bad part of town and you absolutely,
     * positively, don't want nobody peeking at your things, this is the
     * one to use, no problem! After all in a Bouncy Castle nothing can
     * touch you.
     *
     * Also referred to by the alias UBER.
     */
    public static class BouncyCastleStore
        extends JDKKeyStore
    {
        public void engineLoad(
            InputStream stream,
            char[]      password) 
            throws IOException
        {
            table.clear();
    
            if (stream == null)     // just initialising
            {
                return;
            }
    
            DataInputStream     dIn = new DataInputStream(stream);
            int                 version = dIn.readInt();
    
            if (version != STORE_VERSION)
            {
                if (version != 0)
                {
                    throw new IOException("Wrong version of key store.");
                }
            }
    
            byte[]      salt = new byte[dIn.readInt()];

            if (salt.length != STORE_SALT_SIZE)
            {
                throw new IOException("Key store corrupted.");
            }
    
            dIn.readFully(salt);
    
            int         iterationCount = dIn.readInt();
    
            if ((iterationCount < 0) || (iterationCount > 4 *  MIN_ITERATIONS))
            {
                throw new IOException("Key store corrupted.");
            }
    
            String cipherAlg;
            if (version == 0)
            {
                cipherAlg = "Old" + STORE_CIPHER;
            }
            else
            {
                cipherAlg = STORE_CIPHER;
            }

            Cipher cipher = this.makePBECipher(cipherAlg, Cipher.DECRYPT_MODE, password, salt, iterationCount);
            CipherInputStream cIn = new CipherInputStream(dIn, cipher);

            Digest dig = new SHA1Digest();
            DigestInputStream  dgIn = new DigestInputStream(cIn, dig);
    
            this.loadStore(dgIn);

            // Finalise our digest calculation
            byte[] hash = new byte[dig.getDigestSize()];
            dig.doFinal(hash, 0);

            // TODO Should this actually be reading the remainder of the stream?
            // Read the original digest from the stream
            byte[] oldHash = new byte[dig.getDigestSize()];
            Streams.readFully(cIn, oldHash);

            if (!Arrays.constantTimeAreEqual(hash, oldHash))
            {
                table.clear();
                throw new IOException("KeyStore integrity check failed.");
            }
        }
    
    
        public void engineStore(OutputStream stream, char[] password) 
            throws IOException
        {
            Cipher              cipher;
            DataOutputStream    dOut = new DataOutputStream(stream);
            byte[]              salt = new byte[STORE_SALT_SIZE];
            int                 iterationCount = MIN_ITERATIONS + (random.nextInt() & 0x3ff);
    
            random.nextBytes(salt);
    
            dOut.writeInt(STORE_VERSION);
            dOut.writeInt(salt.length);
            dOut.write(salt);
            dOut.writeInt(iterationCount);
    
            cipher = this.makePBECipher(STORE_CIPHER, Cipher.ENCRYPT_MODE, password, salt, iterationCount);
    
            CipherOutputStream  cOut = new CipherOutputStream(dOut, cipher);
            DigestOutputStream  dgOut = new DigestOutputStream(cOut, new SHA1Digest());
    
            this.saveStore(dgOut);
    
            Digest  dig = dgOut.getDigest();
            byte[]  hash = new byte[dig.getDigestSize()];
    
            dig.doFinal(hash, 0);
    
            cOut.write(hash);
    
            cOut.close();
        }
    }
}
