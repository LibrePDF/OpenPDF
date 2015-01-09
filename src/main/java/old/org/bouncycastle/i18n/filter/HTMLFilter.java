
package old.org.bouncycastle.i18n.filter;

/**
 * HTML Filter
 */
public class HTMLFilter implements Filter 
{

    public String doFilter(String input) 
    {
        StringBuffer buf = new StringBuffer(input);
        int i = 0;
        while (i < buf.length()) 
        {
            char ch = buf.charAt(i);
            switch (ch)
            {
            case '<':
                buf.replace(i,i+1,"&#60");
                break;
            case '>':
                buf.replace(i,i+1,"&#62");
                break;
            case '(':
                buf.replace(i,i+1,"&#40");
                break;
            case ')':
                buf.replace(i,i+1,"&#41");
                break;
            case '#':
                buf.replace(i,i+1,"&#35");
                break;
            case '&':
                buf.replace(i,i+1,"&#38");
                break;
            case '\"':
                buf.replace(i,i+1,"&#34");
                break;
            case '\'':
                buf.replace(i,i+1,"&#39");
                break;
            case '%':
                buf.replace(i,i+1,"&#37");
                break;
            case ';':
                buf.replace(i,i+1,"&#59");
                break;
            case '+':
                buf.replace(i,i+1,"&#43");
                break;
            case '-':
                buf.replace(i,i+1,"&#45");
                break;
            default:
                i -= 3;
            }
            i += 4;
        }
        return buf.toString();
    }
    
    public String doFilterUrl(String input)
    {
        return doFilter(input);
    }

}
