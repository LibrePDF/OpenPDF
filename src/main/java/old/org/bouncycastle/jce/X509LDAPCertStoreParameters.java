package old.org.bouncycastle.jce;

import old.org.bouncycastle.x509.X509StoreParameters;

import java.security.cert.CertStoreParameters;
import java.security.cert.LDAPCertStoreParameters;

/**
 * An expanded set of parameters for an LDAPCertStore
 */
public class X509LDAPCertStoreParameters
    implements X509StoreParameters, CertStoreParameters
{

    private String ldapURL;

    private String baseDN;

    // LDAP attributes, where data is stored

    private String userCertificateAttribute;

    private String cACertificateAttribute;

    private String crossCertificateAttribute;

    private String certificateRevocationListAttribute;

    private String deltaRevocationListAttribute;

    private String authorityRevocationListAttribute;

    private String attributeCertificateAttributeAttribute;

    private String aACertificateAttribute;

    private String attributeDescriptorCertificateAttribute;

    private String attributeCertificateRevocationListAttribute;

    private String attributeAuthorityRevocationListAttribute;

    // LDAP attributes with which data can be found

    private String ldapUserCertificateAttributeName;

    private String ldapCACertificateAttributeName;

    private String ldapCrossCertificateAttributeName;

    private String ldapCertificateRevocationListAttributeName;

    private String ldapDeltaRevocationListAttributeName;

    private String ldapAuthorityRevocationListAttributeName;

    private String ldapAttributeCertificateAttributeAttributeName;

    private String ldapAACertificateAttributeName;

    private String ldapAttributeDescriptorCertificateAttributeName;

    private String ldapAttributeCertificateRevocationListAttributeName;

    private String ldapAttributeAuthorityRevocationListAttributeName;

    // certificates and CRLs subject or issuer DN attributes, which must be
    // matched against ldap attribute names

    private String userCertificateSubjectAttributeName;

    private String cACertificateSubjectAttributeName;

    private String crossCertificateSubjectAttributeName;

    private String certificateRevocationListIssuerAttributeName;

    private String deltaRevocationListIssuerAttributeName;

    private String authorityRevocationListIssuerAttributeName;

    private String attributeCertificateAttributeSubjectAttributeName;

    private String aACertificateSubjectAttributeName;

    private String attributeDescriptorCertificateSubjectAttributeName;

    private String attributeCertificateRevocationListIssuerAttributeName;

    private String attributeAuthorityRevocationListIssuerAttributeName;

    private String searchForSerialNumberIn;

    public static class Builder
    {
        private String ldapURL;

        private String baseDN;

        // LDAP attributes, where data is stored

        private String userCertificateAttribute;

        private String cACertificateAttribute;

        private String crossCertificateAttribute;

        private String certificateRevocationListAttribute;

        private String deltaRevocationListAttribute;

        private String authorityRevocationListAttribute;

        private String attributeCertificateAttributeAttribute;

        private String aACertificateAttribute;

        private String attributeDescriptorCertificateAttribute;

        private String attributeCertificateRevocationListAttribute;

        private String attributeAuthorityRevocationListAttribute;

        // LDAP attributes with which data can be found

        private String ldapUserCertificateAttributeName;

        private String ldapCACertificateAttributeName;

        private String ldapCrossCertificateAttributeName;

        private String ldapCertificateRevocationListAttributeName;

        private String ldapDeltaRevocationListAttributeName;

        private String ldapAuthorityRevocationListAttributeName;

        private String ldapAttributeCertificateAttributeAttributeName;

        private String ldapAACertificateAttributeName;

        private String ldapAttributeDescriptorCertificateAttributeName;

        private String ldapAttributeCertificateRevocationListAttributeName;

        private String ldapAttributeAuthorityRevocationListAttributeName;

        // certificates and CRLs subject or issuer DN attributes, which must be
        // matched against ldap attribute names

        private String userCertificateSubjectAttributeName;

        private String cACertificateSubjectAttributeName;

        private String crossCertificateSubjectAttributeName;

        private String certificateRevocationListIssuerAttributeName;

        private String deltaRevocationListIssuerAttributeName;

        private String authorityRevocationListIssuerAttributeName;

        private String attributeCertificateAttributeSubjectAttributeName;

        private String aACertificateSubjectAttributeName;

        private String attributeDescriptorCertificateSubjectAttributeName;

        private String attributeCertificateRevocationListIssuerAttributeName;

        private String attributeAuthorityRevocationListIssuerAttributeName;

        private String searchForSerialNumberIn;

        public Builder()
        {
            this("ldap://localhost:389", "");
        }

        public Builder(String ldapURL, String baseDN)
        {
            this.ldapURL = ldapURL;
            if (baseDN == null)
            {
                this.baseDN = "";
            }
            else
            {
                this.baseDN = baseDN;
            }

            this.userCertificateAttribute = "userCertificate";
            this.cACertificateAttribute = "cACertificate";
            this.crossCertificateAttribute = "crossCertificatePair";
            this.certificateRevocationListAttribute = "certificateRevocationList";
            this.deltaRevocationListAttribute = "deltaRevocationList";
            this.authorityRevocationListAttribute = "authorityRevocationList";
            this.attributeCertificateAttributeAttribute = "attributeCertificateAttribute";
            this.aACertificateAttribute = "aACertificate";
            this.attributeDescriptorCertificateAttribute = "attributeDescriptorCertificate";
            this.attributeCertificateRevocationListAttribute = "attributeCertificateRevocationList";
            this.attributeAuthorityRevocationListAttribute = "attributeAuthorityRevocationList";
            this.ldapUserCertificateAttributeName = "cn";
            this.ldapCACertificateAttributeName = "cn ou o";
            this.ldapCrossCertificateAttributeName = "cn ou o";
            this.ldapCertificateRevocationListAttributeName = "cn ou o";
            this.ldapDeltaRevocationListAttributeName = "cn ou o";
            this.ldapAuthorityRevocationListAttributeName = "cn ou o";
            this.ldapAttributeCertificateAttributeAttributeName = "cn";
            this.ldapAACertificateAttributeName = "cn o ou";
            this.ldapAttributeDescriptorCertificateAttributeName = "cn o ou";
            this.ldapAttributeCertificateRevocationListAttributeName = "cn o ou";
            this.ldapAttributeAuthorityRevocationListAttributeName = "cn o ou";
            this.userCertificateSubjectAttributeName = "cn";
            this.cACertificateSubjectAttributeName = "o ou";
            this.crossCertificateSubjectAttributeName = "o ou";
            this.certificateRevocationListIssuerAttributeName = "o ou";
            this.deltaRevocationListIssuerAttributeName = "o ou";
            this.authorityRevocationListIssuerAttributeName = "o ou";
            this.attributeCertificateAttributeSubjectAttributeName = "cn";
            this.aACertificateSubjectAttributeName = "o ou";
            this.attributeDescriptorCertificateSubjectAttributeName = "o ou";
            this.attributeCertificateRevocationListIssuerAttributeName = "o ou";
            this.attributeAuthorityRevocationListIssuerAttributeName = "o ou";
            this.searchForSerialNumberIn = "uid serialNumber cn";
        }

        /**
         * @param userCertificateAttribute       Attribute name(s) in the LDAP directory where end certificates
         *                                       are stored. Separated by space. Defaults to "userCertificate"
         *                                       if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setUserCertificateAttribute(String userCertificateAttribute)
        {
            this.userCertificateAttribute = userCertificateAttribute;

            return this;
        }

        /**
         * @param cACertificateAttribute         Attribute name(s) in the LDAP directory where CA certificates
         *                                       are stored. Separated by space. Defaults to "cACertificate" if
         *                                       <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setCACertificateAttribute(String cACertificateAttribute)
        {
            this.cACertificateAttribute = cACertificateAttribute;

            return this;
        }

        /**
         * @param crossCertificateAttribute      Attribute name(s), where the cross certificates are stored.
         *                                       Separated by space. Defaults to "crossCertificatePair" if
         *                                       <code>null</code>
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setCrossCertificateAttribute(String crossCertificateAttribute)
        {
            this.crossCertificateAttribute = crossCertificateAttribute;

            return this;
        }

        /**
         * @param certificateRevocationListAttribute
         *                                       Attribute name(s) in the LDAP directory where CRLs are stored.
         *                                       Separated by space. Defaults to "certificateRevocationList" if
         *                                       <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setCertificateRevocationListAttribute(String certificateRevocationListAttribute)
        {
            this.certificateRevocationListAttribute = certificateRevocationListAttribute;

            return this;
        }

        /**
         * @param deltaRevocationListAttribute   Attribute name(s) in the LDAP directory where delta RLs are
         *                                       stored. Separated by space. Defaults to "deltaRevocationList"
         *                                       if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setDeltaRevocationListAttribute(String deltaRevocationListAttribute)
        {
            this.deltaRevocationListAttribute = deltaRevocationListAttribute;

            return this;
        }

        /**
         * @param authorityRevocationListAttribute
         *                                       Attribute name(s) in the LDAP directory where CRLs for
         *                                       authorities are stored. Separated by space. Defaults to
         *                                       "authorityRevocationList" if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAuthorityRevocationListAttribute(String authorityRevocationListAttribute)
        {
            this.authorityRevocationListAttribute = authorityRevocationListAttribute;

            return this;
        }

        /**
         * @param attributeCertificateAttributeAttribute
         *                                       Attribute name(s) in the LDAP directory where end attribute
         *                                       certificates are stored. Separated by space. Defaults to
         *                                       "attributeCertificateAttribute" if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeCertificateAttributeAttribute(String attributeCertificateAttributeAttribute)
        {
            this.attributeCertificateAttributeAttribute = attributeCertificateAttributeAttribute;

            return this;
        }

        /**
         * @param aACertificateAttribute         Attribute name(s) in the LDAP directory where attribute
         *                                       certificates for attribute authorities are stored. Separated
         *                                       by space. Defaults to "aACertificate" if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAACertificateAttribute(String aACertificateAttribute)
        {
            this.aACertificateAttribute = aACertificateAttribute;

            return this;
        }

        /**
         * @param attributeDescriptorCertificateAttribute
         *                                       Attribute name(s) in the LDAP directory where self signed
         *                                       attribute certificates for attribute authorities are stored.
         *                                       Separated by space. Defaults to
         *                                       "attributeDescriptorCertificate" if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeDescriptorCertificateAttribute(String attributeDescriptorCertificateAttribute)
        {
            this.attributeDescriptorCertificateAttribute = attributeDescriptorCertificateAttribute;

            return this;
        }

        /**
         * @param attributeCertificateRevocationListAttribute
         *                                       Attribute name(s) in the LDAP directory where CRLs for
         *                                       attribute certificates are stored. Separated by space.
         *                                       Defaults to "attributeCertificateRevocationList" if
         *                                       <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeCertificateRevocationListAttribute(String attributeCertificateRevocationListAttribute)
        {
            this.attributeCertificateRevocationListAttribute = attributeCertificateRevocationListAttribute;

            return this;
        }

        /**
         * @param attributeAuthorityRevocationListAttribute
         *                                       Attribute name(s) in the LDAP directory where RLs for
         *                                       attribute authority attribute certificates are stored.
         *                                       Separated by space. Defaults to
         *                                       "attributeAuthorityRevocationList" if <code>null</code>.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeAuthorityRevocationListAttribute(String attributeAuthorityRevocationListAttribute)
        {
            this.attributeAuthorityRevocationListAttribute = attributeAuthorityRevocationListAttribute;

            return this;
        }

        /**
         * @param ldapUserCertificateAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search
         *                                       for the attribute value of the specified
         *                                       <code>userCertificateSubjectAttributeName</code>. E.g. if
         *                                       "cn" is used to put information about the subject for end
         *                                       certificates, then specify "cn".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapUserCertificateAttributeName(String ldapUserCertificateAttributeName)
        {
            this.ldapUserCertificateAttributeName = ldapUserCertificateAttributeName;

            return this;
        }

        /**
         * @param ldapCACertificateAttributeName The attribute name(s) in the LDAP directory where to search
         *                                       for the attribute value of the specified
         *                                       <code>cACertificateSubjectAttributeName</code>. E.g. if
         *                                       "ou" is used to put information about the subject for CA
         *                                       certificates, then specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapCACertificateAttributeName(String ldapCACertificateAttributeName)
        {
            this.ldapCACertificateAttributeName = ldapCACertificateAttributeName;

            return this;
        }

        /**
         * @param ldapCrossCertificateAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>crossCertificateSubjectAttributeName</code>. E.g. if
         *                                       "o" is used to put information about the subject for cross
         *                                       certificates, then specify "o".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapCrossCertificateAttributeName(String ldapCrossCertificateAttributeName)
        {
            this.ldapCrossCertificateAttributeName = ldapCrossCertificateAttributeName;

            return this;
        }

        /**
         * @param ldapCertificateRevocationListAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>certificateRevocationListIssuerAttributeName</code>.
         *                                       E.g. if "ou" is used to put information about the issuer of
         *                                       CRLs, specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapCertificateRevocationListAttributeName(String ldapCertificateRevocationListAttributeName)
        {
            this.ldapCertificateRevocationListAttributeName = ldapCertificateRevocationListAttributeName;

            return this;
        }

        /**
         * @param ldapDeltaRevocationListAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>deltaRevocationListIssuerAttributeName</code>. E.g.
         *                                       if "ou" is used to put information about the issuer of CRLs,
         *                                       specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapDeltaRevocationListAttributeName(String ldapDeltaRevocationListAttributeName)
        {
            this.ldapDeltaRevocationListAttributeName = ldapDeltaRevocationListAttributeName;

            return this;
        }

        /**
         * @param ldapAuthorityRevocationListAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>authorityRevocationListIssuerAttributeName</code>.
         *                                       E.g. if "ou" is used to put information about the issuer of
         *                                       CRLs, specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapAuthorityRevocationListAttributeName(String ldapAuthorityRevocationListAttributeName)
        {
            this.ldapAuthorityRevocationListAttributeName = ldapAuthorityRevocationListAttributeName;

            return this;
        }

        /**
         * @param ldapAttributeCertificateAttributeAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>attributeCertificateAttributeSubjectAttributeName</code>.
         *                                       E.g. if "cn" is used to put information about the subject of
         *                                       end attribute certificates, specify "cn".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapAttributeCertificateAttributeAttributeName(String ldapAttributeCertificateAttributeAttributeName)
        {
            this.ldapAttributeCertificateAttributeAttributeName = ldapAttributeCertificateAttributeAttributeName;

            return this;
        }

        /**
         * @param ldapAACertificateAttributeName The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>aACertificateSubjectAttributeName</code>. E.g. if
         *                                       "ou" is used to put information about the subject of attribute
         *                                       authority attribute certificates, specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapAACertificateAttributeName(String ldapAACertificateAttributeName)
        {
            this.ldapAACertificateAttributeName = ldapAACertificateAttributeName;

            return this;
        }

        /**
         * @param ldapAttributeDescriptorCertificateAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>attributeDescriptorCertificateSubjectAttributeName</code>.
         *                                       E.g. if "o" is used to put information about the subject of
         *                                       self signed attribute authority attribute certificates,
         *                                       specify "o".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapAttributeDescriptorCertificateAttributeName(String ldapAttributeDescriptorCertificateAttributeName)
        {
            this.ldapAttributeDescriptorCertificateAttributeName = ldapAttributeDescriptorCertificateAttributeName;

            return this;
        }

        /**
         * @param ldapAttributeCertificateRevocationListAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>attributeCertificateRevocationListIssuerAttributeName</code>.
         *                                       E.g. if "ou" is used to put information about the issuer of
         *                                       CRLs, specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapAttributeCertificateRevocationListAttributeName(String ldapAttributeCertificateRevocationListAttributeName)
        {
            this.ldapAttributeCertificateRevocationListAttributeName = ldapAttributeCertificateRevocationListAttributeName;

            return this;
        }

        /**
         * @param ldapAttributeAuthorityRevocationListAttributeName
         *                                       The attribute name(s) in the LDAP directory where to search for
         *                                       the attribute value of the specified
         *                                       <code>attributeAuthorityRevocationListIssuerAttributeName</code>.
         *                                       E.g. if "ou" is used to put information about the issuer of
         *                                       CRLs, specify "ou".
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setLdapAttributeAuthorityRevocationListAttributeName(String ldapAttributeAuthorityRevocationListAttributeName)
        {
            this.ldapAttributeAuthorityRevocationListAttributeName = ldapAttributeAuthorityRevocationListAttributeName;

            return this;
        }

        /**
         * @param userCertificateSubjectAttributeName
         *                                       Attribute(s) in the subject of the certificate which is used
         *                                       to be searched in the
         *                                       <code>ldapUserCertificateAttributeName</code>. E.g. the
         *                                       "cn" attribute of the DN could be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setUserCertificateSubjectAttributeName(String userCertificateSubjectAttributeName)
        {
            this.userCertificateSubjectAttributeName = userCertificateSubjectAttributeName;

            return this;
        }

        /**
         * @param cACertificateSubjectAttributeName
         *                                       Attribute(s) in the subject of the certificate which is used
         *                                       to be searched in the
         *                                       <code>ldapCACertificateAttributeName</code>. E.g. the "ou"
         *                                       attribute of the DN could be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setCACertificateSubjectAttributeName(String cACertificateSubjectAttributeName)
        {
            this.cACertificateSubjectAttributeName = cACertificateSubjectAttributeName;

            return this;
        }

        /**
         * @param crossCertificateSubjectAttributeName
         *                                       Attribute(s) in the subject of the cross certificate which is
         *                                       used to be searched in the
         *                                       <code>ldapCrossCertificateAttributeName</code>. E.g. the
         *                                       "o" attribute of the DN may be appropriate.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setCrossCertificateSubjectAttributeName(String crossCertificateSubjectAttributeName)
        {
            this.crossCertificateSubjectAttributeName = crossCertificateSubjectAttributeName;

            return this;
        }

        /**
         * @param certificateRevocationListIssuerAttributeName
         *                                       Attribute(s) in the issuer of the CRL which is used to be
         *                                       searched in the
         *                                       <code>ldapCertificateRevocationListAttributeName</code>.
         *                                       E.g. the "o" or "ou" attribute may be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setCertificateRevocationListIssuerAttributeName(String certificateRevocationListIssuerAttributeName)
        {
            this.certificateRevocationListIssuerAttributeName = certificateRevocationListIssuerAttributeName;

            return this;
        }

        /**
         * @param deltaRevocationListIssuerAttributeName
         *                                       Attribute(s) in the issuer of the CRL which is used to be
         *                                       searched in the
         *                                       <code>ldapDeltaRevocationListAttributeName</code>. E.g. the
         *                                       "o" or "ou" attribute may be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setDeltaRevocationListIssuerAttributeName(String deltaRevocationListIssuerAttributeName)
        {
            this.deltaRevocationListIssuerAttributeName = deltaRevocationListIssuerAttributeName;

            return this;
        }

        /**
         * @param authorityRevocationListIssuerAttributeName
         *                                       Attribute(s) in the issuer of the CRL which is used to be
         *                                       searched in the
         *                                       <code>ldapAuthorityRevocationListAttributeName</code>. E.g.
         *                                       the "o" or "ou" attribute may be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAuthorityRevocationListIssuerAttributeName(String authorityRevocationListIssuerAttributeName)
        {
            this.authorityRevocationListIssuerAttributeName = authorityRevocationListIssuerAttributeName;

            return this;
        }

        /**
         * @param attributeCertificateAttributeSubjectAttributeName
         *                                       Attribute(s) in the subject of the attribute certificate which
         *                                       is used to be searched in the
         *                                       <code>ldapAttributeCertificateAttributeAttributeName</code>.
         *                                       E.g. the "cn" attribute of the DN could be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeCertificateAttributeSubjectAttributeName(String attributeCertificateAttributeSubjectAttributeName)
        {
            this.attributeCertificateAttributeSubjectAttributeName = attributeCertificateAttributeSubjectAttributeName;

            return this;
        }

        /**
         * @param aACertificateSubjectAttributeName
         *                                       Attribute(s) in the subject of the attribute certificate which
         *                                       is used to be searched in the
         *                                       <code>ldapAACertificateAttributeName</code>. E.g. the "ou"
         *                                       attribute of the DN could be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAACertificateSubjectAttributeName(String aACertificateSubjectAttributeName)
        {
            this.aACertificateSubjectAttributeName = aACertificateSubjectAttributeName;

            return this;
        }

        /**
         * @param attributeDescriptorCertificateSubjectAttributeName
         *                                       Attribute(s) in the subject of the attribute certificate which
         *                                       is used to be searched in the
         *                                       <code>ldapAttributeDescriptorCertificateAttributeName</code>.
         *                                       E.g. the "o" attribute of the DN could be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeDescriptorCertificateSubjectAttributeName(String attributeDescriptorCertificateSubjectAttributeName)
        {
            this.attributeDescriptorCertificateSubjectAttributeName = attributeDescriptorCertificateSubjectAttributeName;

            return this;
        }

        /**
         * @param attributeCertificateRevocationListIssuerAttributeName
         *                                       Attribute(s) in the issuer of the CRL which is used to be
         *                                       searched in the
         *                                       <code>ldapAttributeCertificateRevocationListAttributeName</code>.
         *                                       E.g. the "o" or "ou" attribute may be used
         *                                       certificate is searched in this LDAP attribute.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeCertificateRevocationListIssuerAttributeName(String attributeCertificateRevocationListIssuerAttributeName)
        {
            this.attributeCertificateRevocationListIssuerAttributeName = attributeCertificateRevocationListIssuerAttributeName;

            return this;
        }

        /**
         * @param attributeAuthorityRevocationListIssuerAttributeName
         *                                       Anttribute(s) in the issuer of the CRL which is used to be
         *                                       searched in the
         *                                       <code>ldapAttributeAuthorityRevocationListAttributeName</code>.
         *                                       E.g. the "o" or "ou" attribute may be used.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setAttributeAuthorityRevocationListIssuerAttributeName(String attributeAuthorityRevocationListIssuerAttributeName)
        {
            this.attributeAuthorityRevocationListIssuerAttributeName = attributeAuthorityRevocationListIssuerAttributeName;

            return this;
        }

        /**
         *
         * @param searchForSerialNumberIn        If not <code>null</code> the serial number of the
         *                                       certificate is searched in this LDAP attribute.
         * @throws IllegalArgumentException if a necessary parameter is <code>null</code>.
         * @return the builder
         */
        public Builder setSearchForSerialNumberIn(String searchForSerialNumberIn)
        {
            this.searchForSerialNumberIn = searchForSerialNumberIn;

            return this;
        }

        public X509LDAPCertStoreParameters build()
        {
             if (ldapUserCertificateAttributeName == null   // migrate to setters
                || ldapCACertificateAttributeName == null
                || ldapCrossCertificateAttributeName == null
                || ldapCertificateRevocationListAttributeName == null
                || ldapDeltaRevocationListAttributeName == null
                || ldapAuthorityRevocationListAttributeName == null
                || ldapAttributeCertificateAttributeAttributeName == null
                || ldapAACertificateAttributeName == null
                || ldapAttributeDescriptorCertificateAttributeName == null
                || ldapAttributeCertificateRevocationListAttributeName == null
                || ldapAttributeAuthorityRevocationListAttributeName == null
                || userCertificateSubjectAttributeName == null
                || cACertificateSubjectAttributeName == null
                || crossCertificateSubjectAttributeName == null
                || certificateRevocationListIssuerAttributeName == null
                || deltaRevocationListIssuerAttributeName == null
                || authorityRevocationListIssuerAttributeName == null
                || attributeCertificateAttributeSubjectAttributeName == null
                || aACertificateSubjectAttributeName == null
                || attributeDescriptorCertificateSubjectAttributeName == null
                || attributeCertificateRevocationListIssuerAttributeName == null
                || attributeAuthorityRevocationListIssuerAttributeName == null)
            {
                throw new IllegalArgumentException(
                    "Necessary parameters not specified.");
            }
            return new X509LDAPCertStoreParameters(this);
        }
    }


    private X509LDAPCertStoreParameters(Builder builder)
    {
        this.ldapURL = builder.ldapURL;
        this.baseDN = builder.baseDN;

        this.userCertificateAttribute = builder.userCertificateAttribute;
        this.cACertificateAttribute = builder.cACertificateAttribute;
        this.crossCertificateAttribute = builder.crossCertificateAttribute;
        this.certificateRevocationListAttribute = builder.certificateRevocationListAttribute;
        this.deltaRevocationListAttribute = builder.deltaRevocationListAttribute;
        this.authorityRevocationListAttribute = builder.authorityRevocationListAttribute;
        this.attributeCertificateAttributeAttribute = builder.attributeCertificateAttributeAttribute;
        this.aACertificateAttribute = builder.aACertificateAttribute;
        this.attributeDescriptorCertificateAttribute = builder.attributeDescriptorCertificateAttribute;
        this.attributeCertificateRevocationListAttribute = builder.attributeCertificateRevocationListAttribute;
        this.attributeAuthorityRevocationListAttribute = builder.attributeAuthorityRevocationListAttribute;
        this.ldapUserCertificateAttributeName = builder.ldapUserCertificateAttributeName;
        this.ldapCACertificateAttributeName = builder.ldapCACertificateAttributeName;
        this.ldapCrossCertificateAttributeName = builder.ldapCrossCertificateAttributeName;
        this.ldapCertificateRevocationListAttributeName = builder.ldapCertificateRevocationListAttributeName;
        this.ldapDeltaRevocationListAttributeName = builder.ldapDeltaRevocationListAttributeName;
        this.ldapAuthorityRevocationListAttributeName = builder.ldapAuthorityRevocationListAttributeName;
        this.ldapAttributeCertificateAttributeAttributeName = builder.ldapAttributeCertificateAttributeAttributeName;
        this.ldapAACertificateAttributeName = builder.ldapAACertificateAttributeName;
        this.ldapAttributeDescriptorCertificateAttributeName = builder.ldapAttributeDescriptorCertificateAttributeName;
        this.ldapAttributeCertificateRevocationListAttributeName = builder.ldapAttributeCertificateRevocationListAttributeName;
        this.ldapAttributeAuthorityRevocationListAttributeName = builder.ldapAttributeAuthorityRevocationListAttributeName;
        this.userCertificateSubjectAttributeName = builder.userCertificateSubjectAttributeName;
        this.cACertificateSubjectAttributeName = builder.cACertificateSubjectAttributeName;
        this.crossCertificateSubjectAttributeName = builder.crossCertificateSubjectAttributeName;
        this.certificateRevocationListIssuerAttributeName = builder.certificateRevocationListIssuerAttributeName;
        this.deltaRevocationListIssuerAttributeName = builder.deltaRevocationListIssuerAttributeName;
        this.authorityRevocationListIssuerAttributeName = builder.authorityRevocationListIssuerAttributeName;
        this.attributeCertificateAttributeSubjectAttributeName = builder.attributeCertificateAttributeSubjectAttributeName;
        this.aACertificateSubjectAttributeName = builder.aACertificateSubjectAttributeName;
        this.attributeDescriptorCertificateSubjectAttributeName = builder.attributeDescriptorCertificateSubjectAttributeName;
        this.attributeCertificateRevocationListIssuerAttributeName = builder.attributeCertificateRevocationListIssuerAttributeName;
        this.attributeAuthorityRevocationListIssuerAttributeName = builder.attributeAuthorityRevocationListIssuerAttributeName;
        this.searchForSerialNumberIn = builder.searchForSerialNumberIn;
    }

    /**
     * Returns a clone of this object.
     */
    public Object clone()
    {
        return this;
    }

    public boolean equal(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof X509LDAPCertStoreParameters))
        {
            return false;
        }

        X509LDAPCertStoreParameters params = (X509LDAPCertStoreParameters)o;
        return checkField(ldapURL, params.ldapURL)
            && checkField(baseDN, params.baseDN)
            && checkField(userCertificateAttribute, params.userCertificateAttribute)
            && checkField(cACertificateAttribute, params.cACertificateAttribute)
            && checkField(crossCertificateAttribute, params.crossCertificateAttribute)
            && checkField(certificateRevocationListAttribute, params.certificateRevocationListAttribute)
            && checkField(deltaRevocationListAttribute, params.deltaRevocationListAttribute)
            && checkField(authorityRevocationListAttribute, params.authorityRevocationListAttribute)
            && checkField(attributeCertificateAttributeAttribute, params.attributeCertificateAttributeAttribute)
            && checkField(aACertificateAttribute, params.aACertificateAttribute)
            && checkField(attributeDescriptorCertificateAttribute, params.attributeDescriptorCertificateAttribute)
            && checkField(attributeCertificateRevocationListAttribute, params.attributeCertificateRevocationListAttribute)
            && checkField(attributeAuthorityRevocationListAttribute, params.attributeAuthorityRevocationListAttribute)
            && checkField(ldapUserCertificateAttributeName, params.ldapUserCertificateAttributeName)
            && checkField(ldapCACertificateAttributeName, params.ldapCACertificateAttributeName)
            && checkField(ldapCrossCertificateAttributeName, params.ldapCrossCertificateAttributeName)
            && checkField(ldapCertificateRevocationListAttributeName, params.ldapCertificateRevocationListAttributeName)
            && checkField(ldapDeltaRevocationListAttributeName, params.ldapDeltaRevocationListAttributeName)
            && checkField(ldapAuthorityRevocationListAttributeName, params.ldapAuthorityRevocationListAttributeName)
            && checkField(ldapAttributeCertificateAttributeAttributeName, params.ldapAttributeCertificateAttributeAttributeName)
            && checkField(ldapAACertificateAttributeName, params.ldapAACertificateAttributeName)
            && checkField(ldapAttributeDescriptorCertificateAttributeName, params.ldapAttributeDescriptorCertificateAttributeName)
            && checkField(ldapAttributeCertificateRevocationListAttributeName, params.ldapAttributeCertificateRevocationListAttributeName)
            && checkField(ldapAttributeAuthorityRevocationListAttributeName, params.ldapAttributeAuthorityRevocationListAttributeName)
            && checkField(userCertificateSubjectAttributeName, params.userCertificateSubjectAttributeName)
            && checkField(cACertificateSubjectAttributeName, params.cACertificateSubjectAttributeName)
            && checkField(crossCertificateSubjectAttributeName, params.crossCertificateSubjectAttributeName)
            && checkField(certificateRevocationListIssuerAttributeName, params.certificateRevocationListIssuerAttributeName)
            && checkField(deltaRevocationListIssuerAttributeName, params.deltaRevocationListIssuerAttributeName)
            && checkField(authorityRevocationListIssuerAttributeName, params.authorityRevocationListIssuerAttributeName)
            && checkField(attributeCertificateAttributeSubjectAttributeName, params.attributeCertificateAttributeSubjectAttributeName)
            && checkField(aACertificateSubjectAttributeName, params.aACertificateSubjectAttributeName)
            && checkField(attributeDescriptorCertificateSubjectAttributeName, params.attributeDescriptorCertificateSubjectAttributeName)
            && checkField(attributeCertificateRevocationListIssuerAttributeName, params.attributeCertificateRevocationListIssuerAttributeName)
            && checkField(attributeAuthorityRevocationListIssuerAttributeName, params.attributeAuthorityRevocationListIssuerAttributeName)
            && checkField(searchForSerialNumberIn, params.searchForSerialNumberIn);
    }

    private boolean checkField(Object o1, Object o2)
    {
        if (o1 == o2)
        {
            return true;
        }

        if (o1 == null)
        {
            return false;
        }

        return o1.equals(o2);
    }

    public int hashCode()
    {
        int hash = 0;

        hash = addHashCode(hash, userCertificateAttribute);
        hash = addHashCode(hash, cACertificateAttribute);
        hash = addHashCode(hash, crossCertificateAttribute);
        hash = addHashCode(hash, certificateRevocationListAttribute);
        hash = addHashCode(hash, deltaRevocationListAttribute);
        hash = addHashCode(hash, authorityRevocationListAttribute);
        hash = addHashCode(hash, attributeCertificateAttributeAttribute);
        hash = addHashCode(hash, aACertificateAttribute);
        hash = addHashCode(hash, attributeDescriptorCertificateAttribute);
        hash = addHashCode(hash, attributeCertificateRevocationListAttribute);
        hash = addHashCode(hash, attributeAuthorityRevocationListAttribute);
        hash = addHashCode(hash, ldapUserCertificateAttributeName);
        hash = addHashCode(hash, ldapCACertificateAttributeName);
        hash = addHashCode(hash, ldapCrossCertificateAttributeName);
        hash = addHashCode(hash, ldapCertificateRevocationListAttributeName);
        hash = addHashCode(hash, ldapDeltaRevocationListAttributeName);
        hash = addHashCode(hash, ldapAuthorityRevocationListAttributeName);
        hash = addHashCode(hash, ldapAttributeCertificateAttributeAttributeName);
        hash = addHashCode(hash, ldapAACertificateAttributeName);
        hash = addHashCode(hash, ldapAttributeDescriptorCertificateAttributeName);
        hash = addHashCode(hash, ldapAttributeCertificateRevocationListAttributeName);
        hash = addHashCode(hash, ldapAttributeAuthorityRevocationListAttributeName);
        hash = addHashCode(hash, userCertificateSubjectAttributeName);
        hash = addHashCode(hash, cACertificateSubjectAttributeName);
        hash = addHashCode(hash, crossCertificateSubjectAttributeName);
        hash = addHashCode(hash, certificateRevocationListIssuerAttributeName);
        hash = addHashCode(hash, deltaRevocationListIssuerAttributeName);
        hash = addHashCode(hash, authorityRevocationListIssuerAttributeName);
        hash = addHashCode(hash, attributeCertificateAttributeSubjectAttributeName);
        hash = addHashCode(hash, aACertificateSubjectAttributeName);
        hash = addHashCode(hash, attributeDescriptorCertificateSubjectAttributeName);
        hash = addHashCode(hash, attributeCertificateRevocationListIssuerAttributeName);
        hash = addHashCode(hash, attributeAuthorityRevocationListIssuerAttributeName);
        hash = addHashCode(hash, searchForSerialNumberIn);
        
        return hash;
    }

    private int addHashCode(int hashCode, Object o)
    {
        return (hashCode * 29) + (o == null ? 0 : o.hashCode());
    }

    /**
     * @return Returns the aACertificateAttribute.
     */
    public String getAACertificateAttribute()
    {
        return aACertificateAttribute;
    }

    /**
     * @return Returns the aACertificateSubjectAttributeName.
     */
    public String getAACertificateSubjectAttributeName()
    {
        return aACertificateSubjectAttributeName;
    }

    /**
     * @return Returns the attributeAuthorityRevocationListAttribute.
     */
    public String getAttributeAuthorityRevocationListAttribute()
    {
        return attributeAuthorityRevocationListAttribute;
    }

    /**
     * @return Returns the attributeAuthorityRevocationListIssuerAttributeName.
     */
    public String getAttributeAuthorityRevocationListIssuerAttributeName()
    {
        return attributeAuthorityRevocationListIssuerAttributeName;
    }

    /**
     * @return Returns the attributeCertificateAttributeAttribute.
     */
    public String getAttributeCertificateAttributeAttribute()
    {
        return attributeCertificateAttributeAttribute;
    }

    /**
     * @return Returns the attributeCertificateAttributeSubjectAttributeName.
     */
    public String getAttributeCertificateAttributeSubjectAttributeName()
    {
        return attributeCertificateAttributeSubjectAttributeName;
    }

    /**
     * @return Returns the attributeCertificateRevocationListAttribute.
     */
    public String getAttributeCertificateRevocationListAttribute()
    {
        return attributeCertificateRevocationListAttribute;
    }

    /**
     * @return Returns the
     *         attributeCertificateRevocationListIssuerAttributeName.
     */
    public String getAttributeCertificateRevocationListIssuerAttributeName()
    {
        return attributeCertificateRevocationListIssuerAttributeName;
    }

    /**
     * @return Returns the attributeDescriptorCertificateAttribute.
     */
    public String getAttributeDescriptorCertificateAttribute()
    {
        return attributeDescriptorCertificateAttribute;
    }

    /**
     * @return Returns the attributeDescriptorCertificateSubjectAttributeName.
     */
    public String getAttributeDescriptorCertificateSubjectAttributeName()
    {
        return attributeDescriptorCertificateSubjectAttributeName;
    }

    /**
     * @return Returns the authorityRevocationListAttribute.
     */
    public String getAuthorityRevocationListAttribute()
    {
        return authorityRevocationListAttribute;
    }

    /**
     * @return Returns the authorityRevocationListIssuerAttributeName.
     */
    public String getAuthorityRevocationListIssuerAttributeName()
    {
        return authorityRevocationListIssuerAttributeName;
    }

    /**
     * @return Returns the baseDN.
     */
    public String getBaseDN()
    {
        return baseDN;
    }

    /**
     * @return Returns the cACertificateAttribute.
     */
    public String getCACertificateAttribute()
    {
        return cACertificateAttribute;
    }

    /**
     * @return Returns the cACertificateSubjectAttributeName.
     */
    public String getCACertificateSubjectAttributeName()
    {
        return cACertificateSubjectAttributeName;
    }

    /**
     * @return Returns the certificateRevocationListAttribute.
     */
    public String getCertificateRevocationListAttribute()
    {
        return certificateRevocationListAttribute;
    }

    /**
     * @return Returns the certificateRevocationListIssuerAttributeName.
     */
    public String getCertificateRevocationListIssuerAttributeName()
    {
        return certificateRevocationListIssuerAttributeName;
    }

    /**
     * @return Returns the crossCertificateAttribute.
     */
    public String getCrossCertificateAttribute()
    {
        return crossCertificateAttribute;
    }

    /**
     * @return Returns the crossCertificateSubjectAttributeName.
     */
    public String getCrossCertificateSubjectAttributeName()
    {
        return crossCertificateSubjectAttributeName;
    }

    /**
     * @return Returns the deltaRevocationListAttribute.
     */
    public String getDeltaRevocationListAttribute()
    {
        return deltaRevocationListAttribute;
    }

    /**
     * @return Returns the deltaRevocationListIssuerAttributeName.
     */
    public String getDeltaRevocationListIssuerAttributeName()
    {
        return deltaRevocationListIssuerAttributeName;
    }

    /**
     * @return Returns the ldapAACertificateAttributeName.
     */
    public String getLdapAACertificateAttributeName()
    {
        return ldapAACertificateAttributeName;
    }

    /**
     * @return Returns the ldapAttributeAuthorityRevocationListAttributeName.
     */
    public String getLdapAttributeAuthorityRevocationListAttributeName()
    {
        return ldapAttributeAuthorityRevocationListAttributeName;
    }

    /**
     * @return Returns the ldapAttributeCertificateAttributeAttributeName.
     */
    public String getLdapAttributeCertificateAttributeAttributeName()
    {
        return ldapAttributeCertificateAttributeAttributeName;
    }

    /**
     * @return Returns the ldapAttributeCertificateRevocationListAttributeName.
     */
    public String getLdapAttributeCertificateRevocationListAttributeName()
    {
        return ldapAttributeCertificateRevocationListAttributeName;
    }

    /**
     * @return Returns the ldapAttributeDescriptorCertificateAttributeName.
     */
    public String getLdapAttributeDescriptorCertificateAttributeName()
    {
        return ldapAttributeDescriptorCertificateAttributeName;
    }

    /**
     * @return Returns the ldapAuthorityRevocationListAttributeName.
     */
    public String getLdapAuthorityRevocationListAttributeName()
    {
        return ldapAuthorityRevocationListAttributeName;
    }

    /**
     * @return Returns the ldapCACertificateAttributeName.
     */
    public String getLdapCACertificateAttributeName()
    {
        return ldapCACertificateAttributeName;
    }

    /**
     * @return Returns the ldapCertificateRevocationListAttributeName.
     */
    public String getLdapCertificateRevocationListAttributeName()
    {
        return ldapCertificateRevocationListAttributeName;
    }

    /**
     * @return Returns the ldapCrossCertificateAttributeName.
     */
    public String getLdapCrossCertificateAttributeName()
    {
        return ldapCrossCertificateAttributeName;
    }

    /**
     * @return Returns the ldapDeltaRevocationListAttributeName.
     */
    public String getLdapDeltaRevocationListAttributeName()
    {
        return ldapDeltaRevocationListAttributeName;
    }

    /**
     * @return Returns the ldapURL.
     */
    public String getLdapURL()
    {
        return ldapURL;
    }

    /**
     * @return Returns the ldapUserCertificateAttributeName.
     */
    public String getLdapUserCertificateAttributeName()
    {
        return ldapUserCertificateAttributeName;
    }

    /**
     * @return Returns the searchForSerialNumberIn.
     */
    public String getSearchForSerialNumberIn()
    {
        return searchForSerialNumberIn;
    }

    /**
     * @return Returns the userCertificateAttribute.
     */
    public String getUserCertificateAttribute()
    {
        return userCertificateAttribute;
    }

    /**
     * @return Returns the userCertificateSubjectAttributeName.
     */
    public String getUserCertificateSubjectAttributeName()
    {
        return userCertificateSubjectAttributeName;
    }

    public static X509LDAPCertStoreParameters getInstance(LDAPCertStoreParameters params)
    {
        String server = "ldap://" + params.getServerName() + ":" + params.getPort();
        X509LDAPCertStoreParameters _params = new Builder(server, "").build();
        return _params;
    }
}
