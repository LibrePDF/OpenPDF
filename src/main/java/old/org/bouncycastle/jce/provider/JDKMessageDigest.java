package old.org.bouncycastle.jce.provider;

import java.security.MessageDigest;

import old.org.bouncycastle.crypto.Digest;
import old.org.bouncycastle.crypto.digests.*;

public class JDKMessageDigest
    extends MessageDigest
{
    Digest  digest;

    protected JDKMessageDigest(
        Digest  digest)
    {
        super(digest.getAlgorithmName());

        this.digest = digest;
    }

    public void engineReset() 
    {
        digest.reset();
    }

    public void engineUpdate(
        byte    input) 
    {
        digest.update(input);
    }

    public void engineUpdate(
        byte[]  input,
        int     offset,
        int     len) 
    {
        digest.update(input, offset, len);
    }

    public byte[] engineDigest() 
    {
        byte[]  digestBytes = new byte[digest.getDigestSize()];

        digest.doFinal(digestBytes, 0);

        return digestBytes;
    }

    /**
     * classes that extend directly off us.
     */
    static public class SHA1
        extends JDKMessageDigest
        implements Cloneable
    {
        public SHA1()
        {
            super(new SHA1Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            SHA1 d = (SHA1)super.clone();
            d.digest = new SHA1Digest((SHA1Digest)digest);

            return d;
        }
    }

    static public class SHA224
        extends JDKMessageDigest
        implements Cloneable
    {
        public SHA224()
        {
            super(new SHA224Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            SHA224 d = (SHA224)super.clone();
            d.digest = new SHA224Digest((SHA224Digest)digest);

            return d;
        }
    }

    static public class SHA256
        extends JDKMessageDigest
        implements Cloneable
    {
        public SHA256()
        {
            super(new SHA256Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            SHA256 d = (SHA256)super.clone();
            d.digest = new SHA256Digest((SHA256Digest)digest);

            return d;
        }
    }

    static public class SHA384
        extends JDKMessageDigest
        implements Cloneable
    {
        public SHA384()
        {
            super(new SHA384Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            SHA384 d = (SHA384)super.clone();
            d.digest = new SHA384Digest((SHA384Digest)digest);

            return d;
        }
    }

    static public class SHA512
        extends JDKMessageDigest
        implements Cloneable
    {
        public SHA512()
        {
            super(new SHA512Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            SHA512 d = (SHA512)super.clone();
            d.digest = new SHA512Digest((SHA512Digest)digest);

            return d;
        }
    }

    static public class MD2
        extends JDKMessageDigest
        implements Cloneable
    {
        public MD2()
        {
            super(new MD2Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            MD2 d = (MD2)super.clone();
            d.digest = new MD2Digest((MD2Digest)digest);

            return d;
        }
    }

    static public class MD4
        extends JDKMessageDigest
        implements Cloneable
    {
        public MD4()
        {
            super(new MD4Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            MD4 d = (MD4)super.clone();
            d.digest = new MD4Digest((MD4Digest)digest);

            return d;
        }
    }

    static public class MD5
        extends JDKMessageDigest
        implements Cloneable
    {
        public MD5()
        {
            super(new MD5Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            MD5 d = (MD5)super.clone();
            d.digest = new MD5Digest((MD5Digest)digest);

            return d;
        }
    }

    static public class RIPEMD128
        extends JDKMessageDigest
        implements Cloneable
    {
        public RIPEMD128()
        {
            super(new RIPEMD128Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            RIPEMD128 d = (RIPEMD128)super.clone();
            d.digest = new RIPEMD128Digest((RIPEMD128Digest)digest);

            return d;
        }
    }

    static public class RIPEMD160
        extends JDKMessageDigest
        implements Cloneable
    {
        public RIPEMD160()
        {
            super(new RIPEMD160Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            RIPEMD160 d = (RIPEMD160)super.clone();
            d.digest = new RIPEMD160Digest((RIPEMD160Digest)digest);

            return d;
        }
    }
    
    static public class RIPEMD256
        extends JDKMessageDigest
        implements Cloneable
    {
        public RIPEMD256()
        {
            super(new RIPEMD256Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            RIPEMD256 d = (RIPEMD256)super.clone();
            d.digest = new RIPEMD256Digest((RIPEMD256Digest)digest);

            return d;
        }
    }
    
    static public class RIPEMD320
        extends JDKMessageDigest
        implements Cloneable
    {
        public RIPEMD320()
        {
            super(new RIPEMD320Digest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            RIPEMD320 d = (RIPEMD320)super.clone();
            d.digest = new RIPEMD320Digest((RIPEMD320Digest)digest);

            return d;
        }
    }
    
    static public class Tiger
        extends JDKMessageDigest
        implements Cloneable
    {
        public Tiger()
        {
            super(new TigerDigest());
        }

        public Object clone()
            throws CloneNotSupportedException
        {
            Tiger d = (Tiger)super.clone();
            d.digest = new TigerDigest((TigerDigest)digest);

            return d;
        }
    }
    
    static public class GOST3411
        extends JDKMessageDigest
        implements Cloneable
    {
        public GOST3411()
        {
            super(new GOST3411Digest());
        }
    
        public Object clone()
        throws CloneNotSupportedException
        {
            GOST3411 d = (GOST3411)super.clone();
            d.digest = new GOST3411Digest((GOST3411Digest)digest);

            return d;
        }
    }
    
    static public class Whirlpool
       extends JDKMessageDigest
       implements Cloneable
    {
        public Whirlpool()
        {
            super(new WhirlpoolDigest());
        }
        
        public Object clone()
        throws CloneNotSupportedException
        {
            Whirlpool d = (Whirlpool)super.clone();
            d.digest = new WhirlpoolDigest((WhirlpoolDigest)digest);
            
            return d;
        }
    }
}
