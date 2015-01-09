
package old.org.bouncycastle.i18n.filter;

/**
 * Filter for strings to store in a SQL table.
 * 
 * escapes ' " = - / \ ; \r \n
 */
public class SQLFilter implements Filter
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
            case '\'':
                buf.replace(i,i+1,"\\\'");
                i += 1;
                break;
            case '\"':
                buf.replace(i,i+1,"\\\"");
                i += 1;
                break;
            case '=':
                buf.replace(i,i+1,"\\=");
                i += 1;
                break;
            case '-':
                buf.replace(i,i+1,"\\-");
                i += 1;
                break;
            case '/':
                buf.replace(i,i+1,"\\/");
                i += 1;
                break;
            case '\\':
                buf.replace(i,i+1,"\\\\");
                i += 1;
                break;
            case ';':
                buf.replace(i,i+1,"\\;");
                i += 1;
                break;
            case '\r':
                buf.replace(i,i+1,"\\r");
                i += 1;
                break;
            case '\n':
                buf.replace(i,i+1,"\\n");
                i += 1;
                break;
            default:
            }
            i++;
        }
        return buf.toString();
    }
    
    public String doFilterUrl(String input)
    {
        return doFilter(input);
    }

}
