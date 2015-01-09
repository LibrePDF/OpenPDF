package old.org.bouncycastle.jce.spec;

import java.math.BigInteger;

/**
 * ParameterSpec for a GOST 3410-94 key parameters.
 */
public class GOST3410PublicKeyParameterSetSpec
{
    private BigInteger p;
    private BigInteger q;
    private BigInteger a;
    
    /**
     * Creates a new GOST3410ParameterSpec with the specified parameter values.
     * 
     * @param p the prime.
     * @param q the sub-prime.
     * @param a the base.
     */
    public GOST3410PublicKeyParameterSetSpec(
        BigInteger p,
        BigInteger q,
        BigInteger a)
    {
        this.p = p;
        this.q = q;
        this.a = a;
    }
    
    /**
     * Returns the prime <code>p</code>.
     *
     * @return the prime <code>p</code>.
     */
    public BigInteger getP() 
    {
        return this.p;
    }
    
    /**
     * Returns the sub-prime <code>q</code>.
     *
     * @return the sub-prime <code>q</code>.
     */
    public BigInteger getQ() 
    {
        return this.q;
    }
    
    /**
     * Returns the base <code>a</code>.
     *
     * @return the base <code>a</code>.
     */
    public BigInteger getA() 
    {
        return this.a;
    }
    
    public boolean equals(
        Object o)
    {
        if (o instanceof GOST3410PublicKeyParameterSetSpec)
        {
            GOST3410PublicKeyParameterSetSpec other = (GOST3410PublicKeyParameterSetSpec)o;
            
            return this.a.equals(other.a) && this.p.equals(other.p) && this.q.equals(other.q);
        }
        
        return false;
    }
    
    public int hashCode()
    {
        return a.hashCode() ^ p.hashCode() ^ q.hashCode();
    }
}
