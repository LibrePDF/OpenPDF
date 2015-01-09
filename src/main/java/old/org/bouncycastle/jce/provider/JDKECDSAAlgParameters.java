package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.DEROctetString;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;

public abstract class JDKECDSAAlgParameters
    extends AlgorithmParametersSpi
{
    public static class SigAlgParameters
        extends JDKAlgorithmParameters
    {
        protected byte[] engineGetEncoded() 
            throws IOException
        {
            return engineGetEncoded("ASN.1");
        }

        protected byte[] engineGetEncoded(
            String format) 
            throws IOException
        {
            if (format == null)
            {
                return engineGetEncoded("ASN.1");
            }
            
            if (format.equals("ASN.1"))
            {
                return new DEROctetString(engineGetEncoded("RAW")).getEncoded();
            }

            return null;
        }

        protected AlgorithmParameterSpec localEngineGetParameterSpec(
            Class paramSpec) 
            throws InvalidParameterSpecException
        {
            throw new InvalidParameterSpecException("unknown parameter spec passed to ECDSA parameters object.");
        }

        protected void engineInit(
            AlgorithmParameterSpec paramSpec) 
            throws InvalidParameterSpecException
        {
            throw new InvalidParameterSpecException("unknown parameter spec passed to ECDSA parameters object.");
        }

        protected void engineInit(
            byte[] params) 
            throws IOException
        {
        }

        protected void engineInit(
            byte[] params,
            String format) 
            throws IOException
        {
            throw new IOException("Unknown parameters format in IV parameters object");
        }

        protected String engineToString() 
        {
            return "ECDSA Parameters";
        }
    }
}
