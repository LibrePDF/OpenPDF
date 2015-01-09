package old.org.bouncycastle.jce.spec;

import java.security.spec.KeySpec;

public class ElGamalKeySpec
    implements KeySpec
{
    private ElGamalParameterSpec  spec;

    public ElGamalKeySpec(
        ElGamalParameterSpec  spec)
    {
        this.spec = spec;
    }

    public ElGamalParameterSpec getParams()
    {
        return spec;
    }
}
