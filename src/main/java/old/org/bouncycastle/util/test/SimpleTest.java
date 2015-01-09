package old.org.bouncycastle.util.test;

import java.io.PrintStream;

import old.org.bouncycastle.util.Arrays;

public abstract class SimpleTest
    implements Test
{
    public abstract String getName();

    private TestResult success()
    {
        return SimpleTestResult.successful(this, "Okay");
    }
    
    protected void fail(
        String message)
    {
        throw new TestFailedException(SimpleTestResult.failed(this, message));
    }
    
    protected void fail(
        String    message,
        Throwable throwable)
    {
        throw new TestFailedException(SimpleTestResult.failed(this, message, throwable));
    }
    
    protected void fail(
        String message,
        Object expected,
        Object found)
    {
        throw new TestFailedException(SimpleTestResult.failed(this, message, expected, found));
    }
        
    protected boolean areEqual(
        byte[] a,
        byte[] b)
    {
        return Arrays.areEqual(a, b);
    }
    
    public TestResult perform()
    {
        try
        {
            performTest();
            
            return success();
        }
        catch (TestFailedException e)
        {
            return e.getResult();
        }
        catch (Exception e)
        {
            return SimpleTestResult.failed(this, "Exception: " +  e, e);
        }
    }
    
    protected static void runTest(
        Test        test)
    {
        runTest(test, System.out);
    }
    
    protected static void runTest(
        Test        test,
        PrintStream out)
    {
        TestResult      result = test.perform();

        out.println(result.toString());
        if (result.getException() != null)
        {
            result.getException().printStackTrace(out);
        }
    }

    public abstract void performTest()
        throws Exception;
}
