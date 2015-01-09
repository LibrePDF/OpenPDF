package old.org.bouncycastle.asn1.esf;

import java.io.IOException;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * OtherRevVals ::= SEQUENCE {
 *    otherRevValType OtherRevValType,
 *    otherRevVals ANY DEFINED BY OtherRevValType
 * }
 * 
 * OtherRevValType ::= OBJECT IDENTIFIER
 * </pre>
 */
public class OtherRevVals extends ASN1Encodable {

	private DERObjectIdentifier otherRevValType;

	private ASN1Object otherRevVals;

	public static OtherRevVals getInstance(Object obj) {
		if (null == obj || obj instanceof OtherRevVals) {
			return (OtherRevVals) obj;
		}
		return new OtherRevVals((ASN1Sequence) obj);
	}

	public OtherRevVals(ASN1Sequence seq) {
		if (seq.size() != 2) {
			throw new IllegalArgumentException("Bad sequence size: "
					+ seq.size());
		}
		this.otherRevValType = (DERObjectIdentifier) seq.getObjectAt(0);
		try {
			this.otherRevVals = ASN1Object.fromByteArray(seq.getObjectAt(1)
					.getDERObject().getDEREncoded());
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	public OtherRevVals(DERObjectIdentifier otherRevValType,
			ASN1Object otherRevVals) {
		this.otherRevValType = otherRevValType;
		this.otherRevVals = otherRevVals;
	}

	public DERObjectIdentifier getOtherRevValType() {
		return this.otherRevValType;
	}

	public ASN1Object getOtherRevVals() {
		return this.otherRevVals;
	}

	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(this.otherRevValType);
		v.add(this.otherRevVals);
		return new DERSequence(v);
	}
}
