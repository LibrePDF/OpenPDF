package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.GeneralSubtree;
import old.org.bouncycastle.util.Arrays;
import old.org.bouncycastle.util.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PKIXNameConstraintValidator
{
    private Set excludedSubtreesDN = new HashSet();

    private Set excludedSubtreesDNS = new HashSet();

    private Set excludedSubtreesEmail = new HashSet();

    private Set excludedSubtreesURI = new HashSet();

    private Set excludedSubtreesIP = new HashSet();

    private Set permittedSubtreesDN;

    private Set permittedSubtreesDNS;

    private Set permittedSubtreesEmail;

    private Set permittedSubtreesURI;

    private Set permittedSubtreesIP;

    public PKIXNameConstraintValidator()
    {
    }

    private static boolean withinDNSubtree(
        ASN1Sequence dns,
        ASN1Sequence subtree)
    {
        if (subtree.size() < 1)
        {
            return false;
        }

        if (subtree.size() > dns.size())
        {
            return false;
        }

        for (int j = subtree.size() - 1; j >= 0; j--)
        {
            if (!subtree.getObjectAt(j).equals(dns.getObjectAt(j)))
            {
                return false;
            }
        }

        return true;
    }

    public void checkPermittedDN(ASN1Sequence dns)
        throws PKIXNameConstraintValidatorException
    {
        checkPermittedDN(permittedSubtreesDN, dns);
    }

    public void checkExcludedDN(ASN1Sequence dns)
        throws PKIXNameConstraintValidatorException
    {
        checkExcludedDN(excludedSubtreesDN, dns);
    }

    private void checkPermittedDN(Set permitted, ASN1Sequence dns)
        throws PKIXNameConstraintValidatorException
    {
        if (permitted == null)
        {
            return;
        }

        if (permitted.isEmpty() && dns.size() == 0)
        {
            return;
        }
        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            ASN1Sequence subtree = (ASN1Sequence)it.next();

            if (withinDNSubtree(dns, subtree))
            {
                return;
            }
        }

        throw new PKIXNameConstraintValidatorException(
            "Subject distinguished name is not from a permitted subtree");
    }

    private void checkExcludedDN(Set excluded, ASN1Sequence dns)
        throws PKIXNameConstraintValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            ASN1Sequence subtree = (ASN1Sequence)it.next();

            if (withinDNSubtree(dns, subtree))
            {
                throw new PKIXNameConstraintValidatorException(
                    "Subject distinguished name is from an excluded subtree");
            }
        }
    }

    private Set intersectDN(Set permitted, Set dns)
    {
        Set intersect = new HashSet();
        for (Iterator it = dns.iterator(); it.hasNext();)
        {
            ASN1Sequence dn = ASN1Sequence.getInstance(((GeneralSubtree)it
                .next()).getBase().getName().getDERObject());
            if (permitted == null)
            {
                if (dn != null)
                {
                    intersect.add(dn);
                }
            }
            else
            {
                Iterator _iter = permitted.iterator();
                while (_iter.hasNext())
                {
                    ASN1Sequence subtree = (ASN1Sequence)_iter.next();

                    if (withinDNSubtree(dn, subtree))
                    {
                        intersect.add(dn);
                    }
                    else if (withinDNSubtree(subtree, dn))
                    {
                        intersect.add(subtree);
                    }
                }
            }
        }
        return intersect;
    }

    private Set unionDN(Set excluded, ASN1Sequence dn)
    {
        if (excluded.isEmpty())
        {
            if (dn == null)
            {
                return excluded;
            }
            excluded.add(dn);

            return excluded;
        }
        else
        {
            Set intersect = new HashSet();

            Iterator it = excluded.iterator();
            while (it.hasNext())
            {
                ASN1Sequence subtree = (ASN1Sequence)it.next();

                if (withinDNSubtree(dn, subtree))
                {
                    intersect.add(subtree);
                }
                else if (withinDNSubtree(subtree, dn))
                {
                    intersect.add(dn);
                }
                else
                {
                    intersect.add(subtree);
                    intersect.add(dn);
                }
            }

            return intersect;
        }
    }

    private Set intersectEmail(Set permitted, Set emails)
    {
        Set intersect = new HashSet();
        for (Iterator it = emails.iterator(); it.hasNext();)
        {
            String email = extractNameAsString(((GeneralSubtree)it.next())
                .getBase());

            if (permitted == null)
            {
                if (email != null)
                {
                    intersect.add(email);
                }
            }
            else
            {
                Iterator it2 = permitted.iterator();
                while (it2.hasNext())
                {
                    String _permitted = (String)it2.next();

                    intersectEmail(email, _permitted, intersect);
                }
            }
        }
        return intersect;
    }

    private Set unionEmail(Set excluded, String email)
    {
        if (excluded.isEmpty())
        {
            if (email == null)
            {
                return excluded;
            }
            excluded.add(email);
            return excluded;
        }
        else
        {
            Set union = new HashSet();

            Iterator it = excluded.iterator();
            while (it.hasNext())
            {
                String _excluded = (String)it.next();

                unionEmail(_excluded, email, union);
            }

            return union;
        }
    }

    /**
     * Returns the intersection of the permitted IP ranges in
     * <code>permitted</code> with <code>ip</code>.
     *
     * @param permitted A <code>Set</code> of permitted IP addresses with
     *                  their subnet mask as byte arrays.
     * @param ips       The IP address with its subnet mask.
     * @return The <code>Set</code> of permitted IP ranges intersected with
     *         <code>ip</code>.
     */
    private Set intersectIP(Set permitted, Set ips)
    {
        Set intersect = new HashSet();
        for (Iterator it = ips.iterator(); it.hasNext();)
        {
            byte[] ip = ASN1OctetString.getInstance(
                ((GeneralSubtree)it.next()).getBase().getName()).getOctets();
            if (permitted == null)
            {
                if (ip != null)
                {
                    intersect.add(ip);
                }
            }
            else
            {
                Iterator it2 = permitted.iterator();
                while (it2.hasNext())
                {
                    byte[] _permitted = (byte[])it2.next();
                    intersect.addAll(intersectIPRange(_permitted, ip));
                }
            }
        }
        return intersect;
    }

    /**
     * Returns the union of the excluded IP ranges in <code>excluded</code>
     * with <code>ip</code>.
     *
     * @param excluded A <code>Set</code> of excluded IP addresses with their
     *                 subnet mask as byte arrays.
     * @param ip       The IP address with its subnet mask.
     * @return The <code>Set</code> of excluded IP ranges unified with
     *         <code>ip</code> as byte arrays.
     */
    private Set unionIP(Set excluded, byte[] ip)
    {
        if (excluded.isEmpty())
        {
            if (ip == null)
            {
                return excluded;
            }
            excluded.add(ip);

            return excluded;
        }
        else
        {
            Set union = new HashSet();

            Iterator it = excluded.iterator();
            while (it.hasNext())
            {
                byte[] _excluded = (byte[])it.next();
                union.addAll(unionIPRange(_excluded, ip));
            }

            return union;
        }
    }

    /**
     * Calculates the union if two IP ranges.
     *
     * @param ipWithSubmask1 The first IP address with its subnet mask.
     * @param ipWithSubmask2 The second IP address with its subnet mask.
     * @return A <code>Set</code> with the union of both addresses.
     */
    private Set unionIPRange(byte[] ipWithSubmask1, byte[] ipWithSubmask2)
    {
        Set set = new HashSet();

        // difficult, adding always all IPs is not wrong
        if (Arrays.areEqual(ipWithSubmask1, ipWithSubmask2))
        {
            set.add(ipWithSubmask1);
        }
        else
        {
            set.add(ipWithSubmask1);
            set.add(ipWithSubmask2);
        }
        return set;
    }

    /**
     * Calculates the interesction if two IP ranges.
     *
     * @param ipWithSubmask1 The first IP address with its subnet mask.
     * @param ipWithSubmask2 The second IP address with its subnet mask.
     * @return A <code>Set</code> with the single IP address with its subnet
     *         mask as a byte array or an empty <code>Set</code>.
     */
    private Set intersectIPRange(byte[] ipWithSubmask1, byte[] ipWithSubmask2)
    {
        if (ipWithSubmask1.length != ipWithSubmask2.length)
        {
            return Collections.EMPTY_SET;
        }
        byte[][] temp = extractIPsAndSubnetMasks(ipWithSubmask1, ipWithSubmask2);
        byte ip1[] = temp[0];
        byte subnetmask1[] = temp[1];
        byte ip2[] = temp[2];
        byte subnetmask2[] = temp[3];

        byte minMax[][] = minMaxIPs(ip1, subnetmask1, ip2, subnetmask2);
        byte[] min;
        byte[] max;
        max = min(minMax[1], minMax[3]);
        min = max(minMax[0], minMax[2]);

        // minimum IP address must be bigger than max
        if (compareTo(min, max) == 1)
        {
            return Collections.EMPTY_SET;
        }
        // OR keeps all significant bits
        byte[] ip = or(minMax[0], minMax[2]);
        byte[] subnetmask = or(subnetmask1, subnetmask2);
        return Collections.singleton(ipWithSubnetMask(ip, subnetmask));
    }

    /**
     * Concatenates the IP address with its subnet mask.
     *
     * @param ip         The IP address.
     * @param subnetMask Its subnet mask.
     * @return The concatenated IP address with its subnet mask.
     */
    private byte[] ipWithSubnetMask(byte[] ip, byte[] subnetMask)
    {
        int ipLength = ip.length;
        byte[] temp = new byte[ipLength * 2];
        System.arraycopy(ip, 0, temp, 0, ipLength);
        System.arraycopy(subnetMask, 0, temp, ipLength, ipLength);
        return temp;
    }

    /**
     * Splits the IP addresses and their subnet mask.
     *
     * @param ipWithSubmask1 The first IP address with the subnet mask.
     * @param ipWithSubmask2 The second IP address with the subnet mask.
     * @return An array with two elements. Each element contains the IP address
     *         and the subnet mask in this order.
     */
    private byte[][] extractIPsAndSubnetMasks(
        byte[] ipWithSubmask1,
        byte[] ipWithSubmask2)
    {
        int ipLength = ipWithSubmask1.length / 2;
        byte ip1[] = new byte[ipLength];
        byte subnetmask1[] = new byte[ipLength];
        System.arraycopy(ipWithSubmask1, 0, ip1, 0, ipLength);
        System.arraycopy(ipWithSubmask1, ipLength, subnetmask1, 0, ipLength);

        byte ip2[] = new byte[ipLength];
        byte subnetmask2[] = new byte[ipLength];
        System.arraycopy(ipWithSubmask2, 0, ip2, 0, ipLength);
        System.arraycopy(ipWithSubmask2, ipLength, subnetmask2, 0, ipLength);
        return new byte[][]
            {ip1, subnetmask1, ip2, subnetmask2};
    }

    /**
     * Based on the two IP addresses and their subnet masks the IP range is
     * computed for each IP address - subnet mask pair and returned as the
     * minimum IP address and the maximum address of the range.
     *
     * @param ip1         The first IP address.
     * @param subnetmask1 The subnet mask of the first IP address.
     * @param ip2         The second IP address.
     * @param subnetmask2 The subnet mask of the second IP address.
     * @return A array with two elements. The first/second element contains the
     *         min and max IP address of the first/second IP address and its
     *         subnet mask.
     */
    private byte[][] minMaxIPs(
        byte[] ip1,
        byte[] subnetmask1,
        byte[] ip2,
        byte[] subnetmask2)
    {
        int ipLength = ip1.length;
        byte[] min1 = new byte[ipLength];
        byte[] max1 = new byte[ipLength];

        byte[] min2 = new byte[ipLength];
        byte[] max2 = new byte[ipLength];

        for (int i = 0; i < ipLength; i++)
        {
            min1[i] = (byte)(ip1[i] & subnetmask1[i]);
            max1[i] = (byte)(ip1[i] & subnetmask1[i] | ~subnetmask1[i]);

            min2[i] = (byte)(ip2[i] & subnetmask2[i]);
            max2[i] = (byte)(ip2[i] & subnetmask2[i] | ~subnetmask2[i]);
        }

        return new byte[][]{min1, max1, min2, max2};
    }

    private void checkPermittedEmail(Set permitted, String email)
        throws PKIXNameConstraintValidatorException
    {
        if (permitted == null)
        {
            return;
        }

        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            String str = ((String)it.next());

            if (emailIsConstrained(email, str))
            {
                return;
            }
        }

        if (email.length() == 0 && permitted.size() == 0)
        {
            return;
        }

        throw new PKIXNameConstraintValidatorException(
            "Subject email address is not from a permitted subtree.");
    }

    private void checkExcludedEmail(Set excluded, String email)
        throws PKIXNameConstraintValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            String str = (String)it.next();

            if (emailIsConstrained(email, str))
            {
                throw new PKIXNameConstraintValidatorException(
                    "Email address is from an excluded subtree.");
            }
        }
    }

    /**
     * Checks if the IP <code>ip</code> is included in the permitted set
     * <code>permitted</code>.
     *
     * @param permitted A <code>Set</code> of permitted IP addresses with
     *                  their subnet mask as byte arrays.
     * @param ip        The IP address.
     * @throws PKIXNameConstraintValidatorException
     *          if the IP is not permitted.
     */
    private void checkPermittedIP(Set permitted, byte[] ip)
        throws PKIXNameConstraintValidatorException
    {
        if (permitted == null)
        {
            return;
        }

        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            byte[] ipWithSubnet = (byte[])it.next();

            if (isIPConstrained(ip, ipWithSubnet))
            {
                return;
            }
        }
        if (ip.length == 0 && permitted.size() == 0)
        {
            return;
        }
        throw new PKIXNameConstraintValidatorException(
            "IP is not from a permitted subtree.");
    }

    /**
     * Checks if the IP <code>ip</code> is included in the excluded set
     * <code>excluded</code>.
     *
     * @param excluded A <code>Set</code> of excluded IP addresses with their
     *                 subnet mask as byte arrays.
     * @param ip       The IP address.
     * @throws PKIXNameConstraintValidatorException
     *          if the IP is excluded.
     */
    private void checkExcludedIP(Set excluded, byte[] ip)
        throws PKIXNameConstraintValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            byte[] ipWithSubnet = (byte[])it.next();

            if (isIPConstrained(ip, ipWithSubnet))
            {
                throw new PKIXNameConstraintValidatorException(
                    "IP is from an excluded subtree.");
            }
        }
    }

    /**
     * Checks if the IP address <code>ip</code> is constrained by
     * <code>constraint</code>.
     *
     * @param ip         The IP address.
     * @param constraint The constraint. This is an IP address concatenated with
     *                   its subnetmask.
     * @return <code>true</code> if constrained, <code>false</code>
     *         otherwise.
     */
    private boolean isIPConstrained(byte ip[], byte[] constraint)
    {
        int ipLength = ip.length;

        if (ipLength != (constraint.length / 2))
        {
            return false;
        }

        byte[] subnetMask = new byte[ipLength];
        System.arraycopy(constraint, ipLength, subnetMask, 0, ipLength);

        byte[] permittedSubnetAddress = new byte[ipLength];

        byte[] ipSubnetAddress = new byte[ipLength];

        // the resulting IP address by applying the subnet mask
        for (int i = 0; i < ipLength; i++)
        {
            permittedSubnetAddress[i] = (byte)(constraint[i] & subnetMask[i]);
            ipSubnetAddress[i] = (byte)(ip[i] & subnetMask[i]);
        }

        return Arrays.areEqual(permittedSubnetAddress, ipSubnetAddress);
    }

    private boolean emailIsConstrained(String email, String constraint)
    {
        String sub = email.substring(email.indexOf('@') + 1);
        // a particular mailbox
        if (constraint.indexOf('@') != -1)
        {
            if (email.equalsIgnoreCase(constraint))
            {
                return true;
            }
        }
        // on particular host
        else if (!(constraint.charAt(0) == '.'))
        {
            if (sub.equalsIgnoreCase(constraint))
            {
                return true;
            }
        }
        // address in sub domain
        else if (withinDomain(sub, constraint))
        {
            return true;
        }
        return false;
    }

    private boolean withinDomain(String testDomain, String domain)
    {
        String tempDomain = domain;
        if (tempDomain.startsWith("."))
        {
            tempDomain = tempDomain.substring(1);
        }
        String[] domainParts = Strings.split(tempDomain, '.');
        String[] testDomainParts = Strings.split(testDomain, '.');
        // must have at least one subdomain
        if (testDomainParts.length <= domainParts.length)
        {
            return false;
        }
        int d = testDomainParts.length - domainParts.length;
        for (int i = -1; i < domainParts.length; i++)
        {
            if (i == -1)
            {
                if (testDomainParts[i + d].equals(""))
                {
                    return false;
                }
            }
            else if (!domainParts[i].equalsIgnoreCase(testDomainParts[i + d]))
            {
                return false;
            }
        }
        return true;
    }

    private void checkPermittedDNS(Set permitted, String dns)
        throws PKIXNameConstraintValidatorException
    {
        if (permitted == null)
        {
            return;
        }

        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            String str = ((String)it.next());

            // is sub domain
            if (withinDomain(dns, str) || dns.equalsIgnoreCase(str))
            {
                return;
            }
        }
        if (dns.length() == 0 && permitted.size() == 0)
        {
            return;
        }
        throw new PKIXNameConstraintValidatorException(
            "DNS is not from a permitted subtree.");
    }

    private void checkExcludedDNS(Set excluded, String dns)
        throws PKIXNameConstraintValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            String str = ((String)it.next());

            // is sub domain or the same
            if (withinDomain(dns, str) || dns.equalsIgnoreCase(str))
            {
                throw new PKIXNameConstraintValidatorException(
                    "DNS is from an excluded subtree.");
            }
        }
    }

    /**
     * The common part of <code>email1</code> and <code>email2</code> is
     * added to the union <code>union</code>. If <code>email1</code> and
     * <code>email2</code> have nothing in common they are added both.
     *
     * @param email1 Email address constraint 1.
     * @param email2 Email address constraint 2.
     * @param union  The union.
     */
    private void unionEmail(String email1, String email2, Set union)
    {
        // email1 is a particular address
        if (email1.indexOf('@') != -1)
        {
            String _sub = email1.substring(email1.indexOf('@') + 1);
            // both are a particular mailbox
            if (email2.indexOf('@') != -1)
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(_sub, email2))
                {
                    union.add(email2);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (_sub.equalsIgnoreCase(email2))
                {
                    union.add(email2);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
        }
        // email1 specifies a domain
        else if (email1.startsWith("."))
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email1.indexOf('@') + 1);
                if (withinDomain(_sub, email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2)
                    || email1.equalsIgnoreCase(email2))
                {
                    union.add(email2);
                }
                else if (withinDomain(email2, email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            else
            {
                if (withinDomain(email2, email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
        }
        // email specifies a host
        else
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email1.indexOf('@') + 1);
                if (_sub.equalsIgnoreCase(email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2))
                {
                    union.add(email2);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
        }
    }

    private void unionURI(String email1, String email2, Set union)
    {
        // email1 is a particular address
        if (email1.indexOf('@') != -1)
        {
            String _sub = email1.substring(email1.indexOf('@') + 1);
            // both are a particular mailbox
            if (email2.indexOf('@') != -1)
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(_sub, email2))
                {
                    union.add(email2);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (_sub.equalsIgnoreCase(email2))
                {
                    union.add(email2);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
        }
        // email1 specifies a domain
        else if (email1.startsWith("."))
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email1.indexOf('@') + 1);
                if (withinDomain(_sub, email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2)
                    || email1.equalsIgnoreCase(email2))
                {
                    union.add(email2);
                }
                else if (withinDomain(email2, email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            else
            {
                if (withinDomain(email2, email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
        }
        // email specifies a host
        else
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email1.indexOf('@') + 1);
                if (_sub.equalsIgnoreCase(email1))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2))
                {
                    union.add(email2);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    union.add(email1);
                }
                else
                {
                    union.add(email1);
                    union.add(email2);
                }
            }
        }
    }

    private Set intersectDNS(Set permitted, Set dnss)
    {
        Set intersect = new HashSet();
        for (Iterator it = dnss.iterator(); it.hasNext();)
        {
            String dns = extractNameAsString(((GeneralSubtree)it.next())
                .getBase());
            if (permitted == null)
            {
                if (dns != null)
                {
                    intersect.add(dns);
                }
            }
            else
            {
                Iterator _iter = permitted.iterator();
                while (_iter.hasNext())
                {
                    String _permitted = (String)_iter.next();

                    if (withinDomain(_permitted, dns))
                    {
                        intersect.add(_permitted);
                    }
                    else if (withinDomain(dns, _permitted))
                    {
                        intersect.add(dns);
                    }
                }
            }
        }

        return intersect;
    }

    protected Set unionDNS(Set excluded, String dns)
    {
        if (excluded.isEmpty())
        {
            if (dns == null)
            {
                return excluded;
            }
            excluded.add(dns);

            return excluded;
        }
        else
        {
            Set union = new HashSet();

            Iterator _iter = excluded.iterator();
            while (_iter.hasNext())
            {
                String _permitted = (String)_iter.next();

                if (withinDomain(_permitted, dns))
                {
                    union.add(dns);
                }
                else if (withinDomain(dns, _permitted))
                {
                    union.add(_permitted);
                }
                else
                {
                    union.add(_permitted);
                    union.add(dns);
                }
            }

            return union;
        }
    }

    /**
     * The most restricting part from <code>email1</code> and
     * <code>email2</code> is added to the intersection <code>intersect</code>.
     *
     * @param email1    Email address constraint 1.
     * @param email2    Email address constraint 2.
     * @param intersect The intersection.
     */
    private void intersectEmail(String email1, String email2, Set intersect)
    {
        // email1 is a particular address
        if (email1.indexOf('@') != -1)
        {
            String _sub = email1.substring(email1.indexOf('@') + 1);
            // both are a particular mailbox
            if (email2.indexOf('@') != -1)
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(_sub, email2))
                {
                    intersect.add(email1);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (_sub.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
            }
        }
        // email specifies a domain
        else if (email1.startsWith("."))
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email1.indexOf('@') + 1);
                if (withinDomain(_sub, email1))
                {
                    intersect.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2)
                    || email1.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
                else if (withinDomain(email2, email1))
                {
                    intersect.add(email2);
                }
            }
            else
            {
                if (withinDomain(email2, email1))
                {
                    intersect.add(email2);
                }
            }
        }
        // email1 specifies a host
        else
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email2.indexOf('@') + 1);
                if (_sub.equalsIgnoreCase(email1))
                {
                    intersect.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2))
                {
                    intersect.add(email1);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
            }
        }
    }

    private void checkExcludedURI(Set excluded, String uri)
        throws PKIXNameConstraintValidatorException
    {
        if (excluded.isEmpty())
        {
            return;
        }

        Iterator it = excluded.iterator();

        while (it.hasNext())
        {
            String str = ((String)it.next());

            if (isUriConstrained(uri, str))
            {
                throw new PKIXNameConstraintValidatorException(
                    "URI is from an excluded subtree.");
            }
        }
    }

    private Set intersectURI(Set permitted, Set uris)
    {
        Set intersect = new HashSet();
        for (Iterator it = uris.iterator(); it.hasNext();)
        {
            String uri = extractNameAsString(((GeneralSubtree)it.next())
                .getBase());
            if (permitted == null)
            {
                if (uri != null)
                {
                    intersect.add(uri);
                }
            }
            else
            {
                Iterator _iter = permitted.iterator();
                while (_iter.hasNext())
                {
                    String _permitted = (String)_iter.next();
                    intersectURI(_permitted, uri, intersect);
                }
            }
        }
        return intersect;
    }

    private Set unionURI(Set excluded, String uri)
    {
        if (excluded.isEmpty())
        {
            if (uri == null)
            {
                return excluded;
            }
            excluded.add(uri);

            return excluded;
        }
        else
        {
            Set union = new HashSet();

            Iterator _iter = excluded.iterator();
            while (_iter.hasNext())
            {
                String _excluded = (String)_iter.next();

                unionURI(_excluded, uri, union);
            }

            return union;
        }
    }

    private void intersectURI(String email1, String email2, Set intersect)
    {
        // email1 is a particular address
        if (email1.indexOf('@') != -1)
        {
            String _sub = email1.substring(email1.indexOf('@') + 1);
            // both are a particular mailbox
            if (email2.indexOf('@') != -1)
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(_sub, email2))
                {
                    intersect.add(email1);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (_sub.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
            }
        }
        // email specifies a domain
        else if (email1.startsWith("."))
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email1.indexOf('@') + 1);
                if (withinDomain(_sub, email1))
                {
                    intersect.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2)
                    || email1.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
                else if (withinDomain(email2, email1))
                {
                    intersect.add(email2);
                }
            }
            else
            {
                if (withinDomain(email2, email1))
                {
                    intersect.add(email2);
                }
            }
        }
        // email1 specifies a host
        else
        {
            if (email2.indexOf('@') != -1)
            {
                String _sub = email2.substring(email2.indexOf('@') + 1);
                if (_sub.equalsIgnoreCase(email1))
                {
                    intersect.add(email2);
                }
            }
            // email2 specifies a domain
            else if (email2.startsWith("."))
            {
                if (withinDomain(email1, email2))
                {
                    intersect.add(email1);
                }
            }
            // email2 specifies a particular host
            else
            {
                if (email1.equalsIgnoreCase(email2))
                {
                    intersect.add(email1);
                }
            }
        }
    }

    private void checkPermittedURI(Set permitted, String uri)
        throws PKIXNameConstraintValidatorException
    {
        if (permitted == null)
        {
            return;
        }

        Iterator it = permitted.iterator();

        while (it.hasNext())
        {
            String str = ((String)it.next());

            if (isUriConstrained(uri, str))
            {
                return;
            }
        }
        if (uri.length() == 0 && permitted.size() == 0)
        {
            return;
        }
        throw new PKIXNameConstraintValidatorException(
            "URI is not from a permitted subtree.");
    }

    private boolean isUriConstrained(String uri, String constraint)
    {
        String host = extractHostFromURL(uri);
        // a host
        if (!constraint.startsWith("."))
        {
            if (host.equalsIgnoreCase(constraint))
            {
                return true;
            }
        }

        // in sub domain or domain
        else if (withinDomain(host, constraint))
        {
            return true;
        }

        return false;
    }

    private static String extractHostFromURL(String url)
    {
        // see RFC 1738
        // remove ':' after protocol, e.g. http:
        String sub = url.substring(url.indexOf(':') + 1);
        // extract host from Common Internet Scheme Syntax, e.g. http://
        if (sub.indexOf("//") != -1)
        {
            sub = sub.substring(sub.indexOf("//") + 2);
        }
        // first remove port, e.g. http://test.com:21
        if (sub.lastIndexOf(':') != -1)
        {
            sub = sub.substring(0, sub.lastIndexOf(':'));
        }
        // remove user and password, e.g. http://john:password@test.com
        sub = sub.substring(sub.indexOf(':') + 1);
        sub = sub.substring(sub.indexOf('@') + 1);
        // remove local parts, e.g. http://test.com/bla
        if (sub.indexOf('/') != -1)
        {
            sub = sub.substring(0, sub.indexOf('/'));
        }
        return sub;
    }

    /**
     * Checks if the given GeneralName is in the permitted set.
     *
     * @param name The GeneralName
     * @throws PKIXNameConstraintValidatorException
     *          If the <code>name</code>
     */
    public void checkPermitted(GeneralName name)
        throws PKIXNameConstraintValidatorException
    {
        switch (name.getTagNo())
        {
            case 1:
                checkPermittedEmail(permittedSubtreesEmail,
                    extractNameAsString(name));
                break;
            case 2:
                checkPermittedDNS(permittedSubtreesDNS, DERIA5String.getInstance(
                    name.getName()).getString());
                break;
            case 4:
                checkPermittedDN(ASN1Sequence.getInstance(name.getName()
                    .getDERObject()));
                break;
            case 6:
                checkPermittedURI(permittedSubtreesURI, DERIA5String.getInstance(
                    name.getName()).getString());
                break;
            case 7:
                byte[] ip = ASN1OctetString.getInstance(name.getName()).getOctets();

                checkPermittedIP(permittedSubtreesIP, ip);
        }
    }

    /**
     * Check if the given GeneralName is contained in the excluded set.
     *
     * @param name The GeneralName.
     * @throws PKIXNameConstraintValidatorException
     *          If the <code>name</code> is
     *          excluded.
     */
    public void checkExcluded(GeneralName name)
        throws PKIXNameConstraintValidatorException
    {
        switch (name.getTagNo())
        {
            case 1:
                checkExcludedEmail(excludedSubtreesEmail, extractNameAsString(name));
                break;
            case 2:
                checkExcludedDNS(excludedSubtreesDNS, DERIA5String.getInstance(
                    name.getName()).getString());
                break;
            case 4:
                checkExcludedDN(ASN1Sequence.getInstance(name.getName()
                    .getDERObject()));
                break;
            case 6:
                checkExcludedURI(excludedSubtreesURI, DERIA5String.getInstance(
                    name.getName()).getString());
                break;
            case 7:
                byte[] ip = ASN1OctetString.getInstance(name.getName()).getOctets();

                checkExcludedIP(excludedSubtreesIP, ip);
        }
    }

    /**
     * Updates the permitted set of these name constraints with the intersection
     * with the given subtree.
     *
     * @param permitted The permitted subtrees
     */

    public void intersectPermittedSubtree(ASN1Sequence permitted)
    {
        Map subtreesMap = new HashMap();

        // group in sets in a map ordered by tag no.
        for (Enumeration e = permitted.getObjects(); e.hasMoreElements();)
        {
            GeneralSubtree subtree = GeneralSubtree.getInstance(e.nextElement());
            Integer tagNo = new Integer(subtree.getBase().getTagNo());
            if (subtreesMap.get(tagNo) == null)
            {
                subtreesMap.put(tagNo, new HashSet());
            }
            ((Set)subtreesMap.get(tagNo)).add(subtree);
        }

        for (Iterator it = subtreesMap.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry)it.next();

            // go through all subtree groups
            switch (((Integer)entry.getKey()).intValue())
            {
                case 1:
                    permittedSubtreesEmail = intersectEmail(permittedSubtreesEmail,
                        (Set)entry.getValue());
                    break;
                case 2:
                    permittedSubtreesDNS = intersectDNS(permittedSubtreesDNS,
                        (Set)entry.getValue());
                    break;
                case 4:
                    permittedSubtreesDN = intersectDN(permittedSubtreesDN,
                        (Set)entry.getValue());
                    break;
                case 6:
                    permittedSubtreesURI = intersectURI(permittedSubtreesURI,
                        (Set)entry.getValue());
                    break;
                case 7:
                    permittedSubtreesIP = intersectIP(permittedSubtreesIP,
                        (Set)entry.getValue());
            }
        }
    }

    private String extractNameAsString(GeneralName name)
    {
        return DERIA5String.getInstance(name.getName()).getString();
    }

    public void intersectEmptyPermittedSubtree(int nameType)
    {
        switch (nameType)
        {
        case 1:
            permittedSubtreesEmail = new HashSet();
            break;
        case 2:
            permittedSubtreesDNS = new HashSet();
            break;
        case 4:
            permittedSubtreesDN = new HashSet();
            break;
        case 6:
            permittedSubtreesURI = new HashSet();
            break;
        case 7:
            permittedSubtreesIP = new HashSet();
        }
    }

    /**
     * Adds a subtree to the excluded set of these name constraints.
     *
     * @param subtree A subtree with an excluded GeneralName.
     */
    public void addExcludedSubtree(GeneralSubtree subtree)
    {
        GeneralName base = subtree.getBase();

        switch (base.getTagNo())
        {
            case 1:
                excludedSubtreesEmail = unionEmail(excludedSubtreesEmail,
                    extractNameAsString(base));
                break;
            case 2:
                excludedSubtreesDNS = unionDNS(excludedSubtreesDNS,
                    extractNameAsString(base));
                break;
            case 4:
                excludedSubtreesDN = unionDN(excludedSubtreesDN,
                    (ASN1Sequence)base.getName().getDERObject());
                break;
            case 6:
                excludedSubtreesURI = unionURI(excludedSubtreesURI,
                    extractNameAsString(base));
                break;
            case 7:
                excludedSubtreesIP = unionIP(excludedSubtreesIP, ASN1OctetString
                    .getInstance(base.getName()).getOctets());
                break;
        }
    }

    /**
     * Returns the maximum IP address.
     *
     * @param ip1 The first IP address.
     * @param ip2 The second IP address.
     * @return The maximum IP address.
     */
    private static byte[] max(byte[] ip1, byte[] ip2)
    {
        for (int i = 0; i < ip1.length; i++)
        {
            if ((ip1[i] & 0xFFFF) > (ip2[i] & 0xFFFF))
            {
                return ip1;
            }
        }
        return ip2;
    }

    /**
     * Returns the minimum IP address.
     *
     * @param ip1 The first IP address.
     * @param ip2 The second IP address.
     * @return The minimum IP address.
     */
    private static byte[] min(byte[] ip1, byte[] ip2)
    {
        for (int i = 0; i < ip1.length; i++)
        {
            if ((ip1[i] & 0xFFFF) < (ip2[i] & 0xFFFF))
            {
                return ip1;
            }
        }
        return ip2;
    }

    /**
     * Compares IP address <code>ip1</code> with <code>ip2</code>. If ip1
     * is equal to ip2 0 is returned. If ip1 is bigger 1 is returned, -1
     * otherwise.
     *
     * @param ip1 The first IP address.
     * @param ip2 The second IP address.
     * @return 0 if ip1 is equal to ip2, 1 if ip1 is bigger, -1 otherwise.
     */
    private static int compareTo(byte[] ip1, byte[] ip2)
    {
        if (Arrays.areEqual(ip1, ip2))
        {
            return 0;
        }
        if (Arrays.areEqual(max(ip1, ip2), ip1))
        {
            return 1;
        }
        return -1;
    }

    /**
     * Returns the logical OR of the IP addresses <code>ip1</code> and
     * <code>ip2</code>.
     *
     * @param ip1 The first IP address.
     * @param ip2 The second IP address.
     * @return The OR of <code>ip1</code> and <code>ip2</code>.
     */
    private static byte[] or(byte[] ip1, byte[] ip2)
    {
        byte[] temp = new byte[ip1.length];
        for (int i = 0; i < ip1.length; i++)
        {
            temp[i] = (byte)(ip1[i] | ip2[i]);
        }
        return temp;
    }

    public int hashCode()
    {
        return hashCollection(excludedSubtreesDN)
            + hashCollection(excludedSubtreesDNS)
            + hashCollection(excludedSubtreesEmail)
            + hashCollection(excludedSubtreesIP)
            + hashCollection(excludedSubtreesURI)
            + hashCollection(permittedSubtreesDN)
            + hashCollection(permittedSubtreesDNS)
            + hashCollection(permittedSubtreesEmail)
            + hashCollection(permittedSubtreesIP)
            + hashCollection(permittedSubtreesURI);
    }

    private int hashCollection(Collection coll)
    {
        if (coll == null)
        {
            return 0;
        }
        int hash = 0;
        Iterator it1 = coll.iterator();
        while (it1.hasNext())
        {
            Object o = it1.next();
            if (o instanceof byte[])
            {
                hash += Arrays.hashCode((byte[])o);
            }
            else
            {
                hash += o.hashCode();
            }
        }
        return hash;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof PKIXNameConstraintValidator))
        {
            return false;
        }
        PKIXNameConstraintValidator constraintValidator = (PKIXNameConstraintValidator)o;
        return collectionsAreEqual(constraintValidator.excludedSubtreesDN, excludedSubtreesDN)
            && collectionsAreEqual(constraintValidator.excludedSubtreesDNS, excludedSubtreesDNS)
            && collectionsAreEqual(constraintValidator.excludedSubtreesEmail, excludedSubtreesEmail)
            && collectionsAreEqual(constraintValidator.excludedSubtreesIP, excludedSubtreesIP)
            && collectionsAreEqual(constraintValidator.excludedSubtreesURI, excludedSubtreesURI)
            && collectionsAreEqual(constraintValidator.permittedSubtreesDN, permittedSubtreesDN)
            && collectionsAreEqual(constraintValidator.permittedSubtreesDNS, permittedSubtreesDNS)
            && collectionsAreEqual(constraintValidator.permittedSubtreesEmail, permittedSubtreesEmail)
            && collectionsAreEqual(constraintValidator.permittedSubtreesIP, permittedSubtreesIP)
            && collectionsAreEqual(constraintValidator.permittedSubtreesURI, permittedSubtreesURI);
    }

    private boolean collectionsAreEqual(Collection coll1, Collection coll2)
    {
        if (coll1 == coll2)
        {
            return true;
        }
        if (coll1 == null || coll2 == null)
        {
            return false;
        }
        if (coll1.size() != coll2.size())
        {
            return false;
        }
        Iterator it1 = coll1.iterator();

        while (it1.hasNext())
        {
            Object a = it1.next();
            Iterator it2 = coll2.iterator();
            boolean found = false;
            while (it2.hasNext())
            {
                Object b = it2.next();
                if (equals(a, b))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                return false;
            }
        }
        return true;
    }

    private boolean equals(Object o1, Object o2)
    {
        if (o1 == o2)
        {
            return true;
        }
        if (o1 == null || o2 == null)
        {
            return false;
        }
        if (o1 instanceof byte[] && o2 instanceof byte[])
        {
            return Arrays.areEqual((byte[])o1, (byte[])o2);
        }
        else
        {
            return o1.equals(o2);
        }
    }

    /**
     * Stringifies an IPv4 or v6 address with subnet mask.
     *
     * @param ip The IP with subnet mask.
     * @return The stringified IP address.
     */
    private String stringifyIP(byte[] ip)
    {
        String temp = "";
        for (int i = 0; i < ip.length / 2; i++)
        {
            temp += Integer.toString(ip[i] & 0x00FF) + ".";
        }
        temp = temp.substring(0, temp.length() - 1);
        temp += "/";
        for (int i = ip.length / 2; i < ip.length; i++)
        {
            temp += Integer.toString(ip[i] & 0x00FF) + ".";
        }
        temp = temp.substring(0, temp.length() - 1);
        return temp;
    }

    private String stringifyIPCollection(Set ips)
    {
        String temp = "";
        temp += "[";
        for (Iterator it = ips.iterator(); it.hasNext();)
        {
            temp += stringifyIP((byte[])it.next()) + ",";
        }
        if (temp.length() > 1)
        {
            temp = temp.substring(0, temp.length() - 1);
        }
        temp += "]";
        return temp;
    }

    public String toString()
    {
        String temp = "";
        temp += "permitted:\n";
        if (permittedSubtreesDN != null)
        {
            temp += "DN:\n";
            temp += permittedSubtreesDN.toString() + "\n";
        }
        if (permittedSubtreesDNS != null)
        {
            temp += "DNS:\n";
            temp += permittedSubtreesDNS.toString() + "\n";
        }
        if (permittedSubtreesEmail != null)
        {
            temp += "Email:\n";
            temp += permittedSubtreesEmail.toString() + "\n";
        }
        if (permittedSubtreesURI != null)
        {
            temp += "URI:\n";
            temp += permittedSubtreesURI.toString() + "\n";
        }
        if (permittedSubtreesIP != null)
        {
            temp += "IP:\n";
            temp += stringifyIPCollection(permittedSubtreesIP) + "\n";
        }
        temp += "excluded:\n";
        if (!excludedSubtreesDN.isEmpty())
        {
            temp += "DN:\n";
            temp += excludedSubtreesDN.toString() + "\n";
        }
        if (!excludedSubtreesDNS.isEmpty())
        {
            temp += "DNS:\n";
            temp += excludedSubtreesDNS.toString() + "\n";
        }
        if (!excludedSubtreesEmail.isEmpty())
        {
            temp += "Email:\n";
            temp += excludedSubtreesEmail.toString() + "\n";
        }
        if (!excludedSubtreesURI.isEmpty())
        {
            temp += "URI:\n";
            temp += excludedSubtreesURI.toString() + "\n";
        }
        if (!excludedSubtreesIP.isEmpty())
        {
            temp += "IP:\n";
            temp += stringifyIPCollection(excludedSubtreesIP) + "\n";
        }
        return temp;
    }
}
