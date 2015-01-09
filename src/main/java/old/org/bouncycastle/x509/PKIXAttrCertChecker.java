package old.org.bouncycastle.x509;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;
import java.util.Collection;
import java.util.Set;

public abstract class PKIXAttrCertChecker
    implements Cloneable
{

    /**
     * Returns an immutable <code>Set</code> of X.509 attribute certificate
     * extensions that this <code>PKIXAttrCertChecker</code> supports or
     * <code>null</code> if no extensions are supported.
     * <p>
     * Each element of the set is a <code>String</code> representing the
     * Object Identifier (OID) of the X.509 extension that is supported.
     * <p>
     * All X.509 attribute certificate extensions that a
     * <code>PKIXAttrCertChecker</code> might possibly be able to process
     * should be included in the set.
     * 
     * @return an immutable <code>Set</code> of X.509 extension OIDs (in
     *         <code>String</code> format) supported by this
     *         <code>PKIXAttrCertChecker</code>, or <code>null</code> if no
     *         extensions are supported
     */
    public abstract Set getSupportedExtensions();

    /**
     * Performs checks on the specified attribute certificate. Every handled
     * extension is rmeoved from the <code>unresolvedCritExts</code>
     * collection.
     * 
     * @param attrCert The attribute certificate to be checked.
     * @param certPath The certificate path which belongs to the attribute
     *            certificate issuer public key certificate.
     * @param holderCertPath The certificate path which belongs to the holder
     *            certificate.
     * @param unresolvedCritExts a <code>Collection</code> of OID strings
     *            representing the current set of unresolved critical extensions
     * @throws CertPathValidatorException if the specified attribute certificate
     *             does not pass the check.
     */
    public abstract void check(X509AttributeCertificate attrCert, CertPath certPath,
                                 CertPath holderCertPath, Collection unresolvedCritExts)
        throws CertPathValidatorException;

    /**
     * Returns a clone of this object.
     * 
     * @return a copy of this <code>PKIXAttrCertChecker</code>
     */
    public abstract Object clone();
}
