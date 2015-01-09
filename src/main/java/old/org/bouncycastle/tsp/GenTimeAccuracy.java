package old.org.bouncycastle.tsp;

import java.text.DecimalFormat;

import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.tsp.Accuracy;

public class GenTimeAccuracy
{
    private Accuracy accuracy;

    public GenTimeAccuracy(Accuracy accuracy)
    {
        this.accuracy = accuracy;
    }
    
    public int getSeconds()
    {
        return getTimeComponent(accuracy.getSeconds());
    }

    public int getMillis()
    {
        return getTimeComponent(accuracy.getMillis());
    }

    public int getMicros()
    {
        return getTimeComponent(accuracy.getMicros());
    }

    private int getTimeComponent(
        DERInteger time)
    {
        if (time != null)
        {
            return time.getValue().intValue();
        }

        return 0;
    }
    
    public String toString()
    {
        DecimalFormat formatter = new DecimalFormat("000"); // three integer
                                                            // digits
        return getSeconds() + "." + formatter.format(getMillis()) + formatter.format(getMicros());
    }
}
