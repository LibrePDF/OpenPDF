package old.org.bouncycastle.x509;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class contains a collection for collection based <code>X509Store</code>s.
 * 
 * @see old.org.bouncycastle.x509.X509Store
 * 
 */
public class X509CollectionStoreParameters
    implements X509StoreParameters
{
    private Collection collection;

    /**
     * Constructor.
     * <p>
     * The collection is copied.
     * </p>
     * 
     * @param collection
     *            The collection containing X.509 object types.
     * @throws NullPointerException if <code>collection</code> is <code>null</code>.
     */
    public X509CollectionStoreParameters(Collection collection)
    {
        if (collection == null)
        {
            throw new NullPointerException("collection cannot be null");
        }
        this.collection = collection;
    }

    /**
     * Returns a shallow clone. The returned contents are not copied, so adding
     * or removing objects will effect this.
     * 
     * @return a shallow clone.
     */
    public Object clone()
    {
        return new X509CollectionStoreParameters(collection);
    }
    
    /**
     * Returns a copy of the <code>Collection</code>.
     * 
     * @return The <code>Collection</code>. Is never <code>null</code>.
     */
    public Collection getCollection()
    {
        return new ArrayList(collection);
    }
    
    /**
     * Returns a formatted string describing the parameters.
     * 
     * @return a formatted string describing the parameters
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("X509CollectionStoreParameters: [\n");
        sb.append("  collection: " + collection + "\n");
        sb.append("]");
        return sb.toString();
    }
}
