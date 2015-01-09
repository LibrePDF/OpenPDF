package old.org.bouncycastle.voms;

import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.x509.GeneralName;
import old.org.bouncycastle.asn1.x509.IetfAttrSyntax;
import old.org.bouncycastle.x509.X509Attribute;
import old.org.bouncycastle.x509.X509AttributeCertificate;

import java.util.List;
import java.util.Vector;


/**
 * Representation of the authorization information (VO, server address
 * and list of Fully Qualified Attribute Names, or FQANs) contained in
 * a VOMS attribute certificate.
 */
public class VOMSAttribute
{

    /**
     * The ASN.1 object identifier for VOMS attributes
     */
    public static final String VOMS_ATTR_OID = "1.3.6.1.4.1.8005.100.100.4";
    private X509AttributeCertificate myAC;
    private String myHostPort;
    private String myVo;
    private Vector myStringList = new Vector();
    private Vector myFQANs = new Vector();

    /**
     * Parses the contents of an attribute certificate.<br>
     * <b>NOTE:</b> Cryptographic signatures, time stamps etc. will <b>not</b> be checked.
     *
     * @param ac the attribute certificate to parse for VOMS attributes
     */
    public VOMSAttribute(X509AttributeCertificate ac) 
    {
        if (ac == null) 
        {
            throw new IllegalArgumentException("VOMSAttribute: AttributeCertificate is NULL");
        }

        myAC = ac;

        X509Attribute[] l = ac.getAttributes(VOMS_ATTR_OID);

        if (l == null) 
        {
            return;
        }

        try 
        {
            for (int i = 0; i != l.length; i++) 
            {
                IetfAttrSyntax attr = new IetfAttrSyntax((ASN1Sequence)l[i].getValues()[0]);

                // policyAuthority is on the format <vo>/<host>:<port>
                String url = ((DERIA5String)GeneralName.getInstance(((ASN1Sequence) attr.getPolicyAuthority().getDERObject()).getObjectAt(0)).getName()).getString();
                int idx = url.indexOf("://");

                if ((idx < 0) || (idx == (url.length() - 1)))
                {
                    throw new IllegalArgumentException("Bad encoding of VOMS policyAuthority : [" + url + "]");
                }

                myVo = url.substring(0, idx);
                myHostPort = url.substring(idx + 3);

                if (attr.getValueType() != IetfAttrSyntax.VALUE_OCTETS)
                {
                    throw new IllegalArgumentException(
                        "VOMS attribute values are not encoded as octet strings, policyAuthority = " + url);
                }

                ASN1OctetString[]   values = (ASN1OctetString[])attr.getValues();
                for (int j = 0; j != values.length; j++)        
                {
                    String fqan = new String(values[j].getOctets());
                    FQAN f = new FQAN(fqan);

                    if (!myStringList.contains(fqan) && fqan.startsWith("/" + myVo + "/"))
               {
                        myStringList.add(fqan);
                        myFQANs.add(f);
                    }
                }
            }
        }
        catch (IllegalArgumentException ie) 
        {
            throw ie;
        }
        catch (Exception e) 
        {
            throw new IllegalArgumentException("Badly encoded VOMS extension in AC issued by " +
                ac.getIssuer());
        }
    }

    /**
     * @return The AttributeCertificate containing the VOMS information
     */
    public X509AttributeCertificate getAC()
    {
        return myAC;
    }

    /**
     * @return List of String of the VOMS fully qualified
     * attributes names (FQANs):<br>
     * <code>/vo[/group[/group2...]][/Role=[role]][/Capability=capability]</code>
     */
    public List getFullyQualifiedAttributes()
    {
        return myStringList;
    }

    /**
     * @return List of FQAN of the VOMS fully qualified
     * attributes names (FQANs)
     * @see #FQAN
     */
    public List getListOfFQAN()
    {
        return myFQANs;
    }

    /**
     * Returns the address of the issuing VOMS server, on the form <code>&lt;host&gt;:&lt;port&gt;</code>
     * @return String
     */
    public String getHostPort()
    {
        return myHostPort;
    }

    /**
     * Returns the VO name
     * @return
     */
    public String getVO()
    {
        return myVo;
    }

    public String toString()
    {
        return "VO      :" + myVo + "\n" + "HostPort:" + myHostPort + "\n" + "FQANs   :" + myFQANs;
    }

    /**
     * Inner class providing a container of the group,role,capability
     * information triplet in an FQAN.
     */
    public class FQAN
    {
        String fqan;
        String group;
        String role;
        String capability;

        public FQAN(String fqan)
        {
            this.fqan = fqan;
        }

        public FQAN(String group, String role, String capability)
        {
            this.group = group;
            this.role = role;
            this.capability = capability;
        }

        public String getFQAN()
        {
            if (fqan != null)
            {
                return fqan;
            }

            fqan = group + "/Role=" + ((role != null) ? role : "") +
                ((capability != null) ? ("/Capability=" + capability) : "");

            return fqan;
        }

        protected void split()
        {
            int len = fqan.length();
            int i = fqan.indexOf("/Role=");

            if (i < 0)
            {
                return;
            }

            group = fqan.substring(0, i);

            int j = fqan.indexOf("/Capability=", i + 6);
            String s = (j < 0) ? fqan.substring(i + 6) : fqan.substring(i + 6, j);
            role = (s.length() == 0) ? null : s;
            s = (j < 0) ? null : fqan.substring(j + 12);
            capability = ((s == null) || (s.length() == 0)) ? null : s;
        }

        public String getGroup()
        {
            if ((group == null) && (fqan != null))
            {
                split();
            }

            return group;
        }

        public String getRole()
        {
            if ((group == null) && (fqan != null))
            {
                split();
            }

            return role;
        }

        public String getCapability()   
        {
            if ((group == null) && (fqan != null))
            {
                split();
            }

            return capability;
        }

        public String toString()
        {
            return getFQAN();
        }
    }
}
