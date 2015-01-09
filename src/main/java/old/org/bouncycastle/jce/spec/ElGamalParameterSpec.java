package old.org.bouncycastle.jce.spec;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

public class ElGamalParameterSpec
    implements AlgorithmParameterSpec
{
    private BigInteger  p;
    private BigInteger  g;

    /**
     * Constructs a parameter set for Diffie-Hellman, using a prime modulus
     * <code>p</code> and a base generator <code>g</code>.
     * 
     * @param p the prime modulus
     * @param g the base generator
     */
    public ElGamalParameterSpec(
        BigInteger  p,
        BigInteger  g)
    {
        this.p = p;
        this.g = g;
    }

    /**
     * Returns the prime modulus <code>p</code>.
     *
     * @return the prime modulus <code>p</code>
     */
    public BigInteger getP()
    {
        return p;
    }

    /**
     * Returns the base generator <code>g</code>.
     *
     * @return the base generator <code>g</code>
     */
    public BigInteger getG()
    {
        return g;
    }
}
