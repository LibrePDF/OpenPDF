package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.x509.ReasonFlags;

/**
 * This class helps to handle CRL revocation reasons mask. Each CRL handles a
 * certain set of revocation reasons.
 */
class ReasonsMask
{
    private int _reasons;

    /**
     * Constructs are reason mask with the reasons.
     * 
     * @param reasons The reasons.
     */
    ReasonsMask(int reasons)
    {
        _reasons = reasons;
    }

    /**
     * A reason mask with no reason.
     * 
     */
    ReasonsMask()
    {
        this(0);
    }

    /**
     * A mask with all revocation reasons.
     */
    static final ReasonsMask allReasons = new ReasonsMask(ReasonFlags.aACompromise
            | ReasonFlags.affiliationChanged | ReasonFlags.cACompromise
            | ReasonFlags.certificateHold | ReasonFlags.cessationOfOperation
            | ReasonFlags.keyCompromise | ReasonFlags.privilegeWithdrawn
            | ReasonFlags.unused | ReasonFlags.superseded);

    /**
     * Adds all reasons from the reasons mask to this mask.
     * 
     * @param mask The reasons mask to add.
     */
    void addReasons(ReasonsMask mask)
    {
        _reasons = _reasons | mask.getReasons();
    }

    /**
     * Returns <code>true</code> if this reasons mask contains all possible
     * reasons.
     * 
     * @return <code>true</code> if this reasons mask contains all possible
     *         reasons.
     */
    boolean isAllReasons()
    {
        return _reasons == allReasons._reasons ? true : false;
    }

    /**
     * Intersects this mask with the given reasons mask.
     * 
     * @param mask The mask to intersect with.
     * @return The intersection of this and teh given mask.
     */
    ReasonsMask intersect(ReasonsMask mask)
    {
        ReasonsMask _mask = new ReasonsMask();
        _mask.addReasons(new ReasonsMask(_reasons & mask.getReasons()));
        return _mask;
    }

    /**
     * Returns <code>true</code> if the passed reasons mask has new reasons.
     * 
     * @param mask The reasons mask which should be tested for new reasons.
     * @return <code>true</code> if the passed reasons mask has new reasons.
     */
    boolean hasNewReasons(ReasonsMask mask)
    {
        return ((_reasons | mask.getReasons() ^ _reasons) != 0);
    }

    /**
     * Returns the reasons in this mask.
     * 
     * @return Returns the reasons.
     */
    int getReasons()
    {
        return _reasons;
    }
}
