
package old.org.bouncycastle.i18n.filter;

public interface Filter
{

    /**
     * Runs the filter on the input String and returns the filtered String
     * @param input input String
     * @return filtered String
     */
    public String doFilter(String input);
    
    /**
     * Runs the filter on the input url and returns the filtered String
     * @param input input url String
     * @return filtered String
     */
    public String doFilterUrl(String input);

}
