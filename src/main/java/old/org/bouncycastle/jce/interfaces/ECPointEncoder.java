package old.org.bouncycastle.jce.interfaces;

/**
 * All BC elliptic curve keys implement this interface. You need to
 * cast the key to get access to it.
 * <p>
 * By default BC keys produce encodings without point compression,
 * to turn this on call setPointFormat() with "COMPRESSED".
 */
public interface ECPointEncoder
{
    /**
     * Set the formatting for encoding of points. If the String "UNCOMPRESSED" is passed
     * in point compression will not be used. If the String "COMPRESSED" is passed point
     * compression will be used. The default is "UNCOMPRESSED".
     * 
     * @param style the style to use.
     */
    public void setPointFormat(String style);
}
