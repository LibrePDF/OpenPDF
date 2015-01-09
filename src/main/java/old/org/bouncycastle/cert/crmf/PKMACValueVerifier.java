package old.org.bouncycastle.cert.crmf;

import java.io.IOException;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.cmp.PBMParameter;
import old.org.bouncycastle.asn1.crmf.PKMACValue;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.operator.MacCalculator;
import old.org.bouncycastle.util.Arrays;

class PKMACValueVerifier
{
    private final PKMACBuilder builder;

    public PKMACValueVerifier(PKMACBuilder builder)
    {
        this.builder = builder;
    }

    public boolean isValid(PKMACValue value, char[] password, SubjectPublicKeyInfo keyInfo)
        throws CRMFException
    {
        builder.setParameters(PBMParameter.getInstance(value.getAlgId().getParameters()));
        MacCalculator calculator = builder.build(password);

        OutputStream macOut = calculator.getOutputStream();

        try
        {
            macOut.write(keyInfo.getDEREncoded());

            macOut.close();
        }
        catch (IOException e)
        {
            throw new CRMFException("exception encoding mac input: " + e.getMessage(), e);
        }

        return Arrays.areEqual(calculator.getMac(), value.getValue().getBytes());
    }
}