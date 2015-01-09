package old.org.bouncycastle.jce;

import old.org.bouncycastle.util.Strings;

import java.security.BasicPermission;
import java.security.Permission;
import java.util.StringTokenizer;

/**
 * A permission class to define what can be done with the ConfigurableProvider interface.
 * <p>
 * Available permissions are "threadLocalEcImplicitlyCa" and "ecImplicitlyCa" which allow the setting
 * of the thread local and global ecImplicitlyCa parameters respectively.
 * </p>
 * <p>
 * Examples:
 * <ul>
 * <li>ProviderConfigurationPermission("BC"); // enable all permissions</li>
 * <li>ProviderConfigurationPermission("BC", "threadLocalEcImplicitlyCa"); // enable thread local only</li>
 * <li>ProviderConfigurationPermission("BC", "ecImplicitlyCa"); // enable global setting only</li>
 * <li>ProviderConfigurationPermission("BC", "threadLocalEcImplicitlyCa, ecImplicitlyCa"); // enable both explicitly</li>
 * </ul>
 * <p>
 * Note: permission checks are only enforced if a security manager is present.
 * </p>
 */
public class ProviderConfigurationPermission
    extends BasicPermission
{
    private static final int  THREAD_LOCAL_EC_IMPLICITLY_CA = 0x01;

    private static final int  EC_IMPLICITLY_CA = 0x02;
    private static final int  ALL = THREAD_LOCAL_EC_IMPLICITLY_CA | EC_IMPLICITLY_CA;

    private static final String THREAD_LOCAL_EC_IMPLICITLY_CA_STR = "threadlocalecimplicitlyca";
    private static final String EC_IMPLICITLY_CA_STR = "ecimplicitlyca";
    private static final String ALL_STR = "all";

    private final String actions;
    private final int permissionMask;

    public ProviderConfigurationPermission(String name)
    {
        super(name);
        this.actions = "all";
        this.permissionMask = ALL;
    }

    public ProviderConfigurationPermission(String name, String actions)
    {
        super(name, actions);
        this.actions = actions;
        this.permissionMask = calculateMask(actions);
    }

    private int calculateMask(
        String actions)
    {
        StringTokenizer tok = new StringTokenizer(Strings.toLowerCase(actions), " ,");
        int             mask = 0;

        while (tok.hasMoreTokens())
        {
            String s = tok.nextToken();

            if (s.equals(THREAD_LOCAL_EC_IMPLICITLY_CA_STR))
            {
                mask |= THREAD_LOCAL_EC_IMPLICITLY_CA;
            }
            else if (s.equals(EC_IMPLICITLY_CA_STR))
            {
                mask |= EC_IMPLICITLY_CA;
            }
            else if (s.equals(ALL_STR))
            {
                mask |= ALL;
            }
        }

        if (mask == 0)
        {
            throw new IllegalArgumentException("unknown permissions passed to mask");
        }
        
        return mask;
    }

    public String getActions()
    {
        return actions;
    }

    public boolean implies(
        Permission permission)
    {
        if (!(permission instanceof ProviderConfigurationPermission))
        {
            return false;
        }

        if (!this.getName().equals(permission.getName()))
        {
            return false;
        }
        
        ProviderConfigurationPermission other = (ProviderConfigurationPermission)permission;
        
        return (this.permissionMask & other.permissionMask) == other.permissionMask;
    }

    public boolean equals(
        Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof ProviderConfigurationPermission)
        {
            ProviderConfigurationPermission other = (ProviderConfigurationPermission)obj;

            return this.permissionMask == other.permissionMask && this.getName().equals(other.getName());
        }

        return false;
    }

    public int hashCode()
    {
        return this.getName().hashCode() + this.permissionMask;
    }
}
