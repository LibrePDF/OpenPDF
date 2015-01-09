package old.org.bouncycastle.tsp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;

/**
 * Recognised hash algorithms for the time stamp protocol.
 */
public interface TSPAlgorithms
{
    public static final String MD5 = PKCSObjectIdentifiers.md5.getId();

    public static final String SHA1 = OIWObjectIdentifiers.idSHA1.getId();
    
    public static final String SHA224 = NISTObjectIdentifiers.id_sha224.getId();
    public static final String SHA256 = NISTObjectIdentifiers.id_sha256.getId();
    public static final String SHA384 = NISTObjectIdentifiers.id_sha384.getId();
    public static final String SHA512 = NISTObjectIdentifiers.id_sha512.getId();

    public static final String RIPEMD128 = TeleTrusTObjectIdentifiers.ripemd128.getId();
    public static final String RIPEMD160 = TeleTrusTObjectIdentifiers.ripemd160.getId();
    public static final String RIPEMD256 = TeleTrusTObjectIdentifiers.ripemd256.getId();
    
    public static final String GOST3411 = CryptoProObjectIdentifiers.gostR3411.getId();
    
    public static final Set    ALLOWED = new HashSet(Arrays.asList(new String[] { GOST3411, MD5, SHA1, SHA224, SHA256, SHA384, SHA512, RIPEMD128, RIPEMD160, RIPEMD256 }));
}
