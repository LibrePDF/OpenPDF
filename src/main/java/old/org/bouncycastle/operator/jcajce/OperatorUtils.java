package old.org.bouncycastle.operator.jcajce;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import old.org.bouncycastle.operator.GenericKey;

class OperatorUtils
{
    static Key getJceKey(GenericKey key)
    {
        if (key.getRepresentation() instanceof Key)
        {
            return (Key)key.getRepresentation();
        }

        if (key.getRepresentation() instanceof byte[])
        {
            return new SecretKeySpec((byte[])key.getRepresentation(), "ENC");
        }

        throw new IllegalArgumentException("unknown generic key type");
    }
}