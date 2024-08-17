/* Copyright 2008 Pirion Systems Pty Ltd, 139 Warry St,
 * Fortitude Valley, Queensland, Australia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.sun.pdfview.decrypt;

import com.sun.pdfview.PDFDocCharsetEncoder;
import com.sun.pdfview.Identity8BitCharsetEncoder;
import com.sun.pdfview.PDFStringUtil;

import java.util.*;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

/**
 * <p>Identifies a PDF Password, expressible either as a string or a
 * byte sequence.</p>
 *
 * <p>In revisions up to version 1.e Expansion 3, the mapping between a string
 * and the bytes corresponding to the password was poorly specified, meaning
 * that the safest manner in which to specify a password was via a byte array.
 * With 1.7 expansion 3, a still slightly problematic mapping was given for the
 * Standard encryption algorithms through to version 4, and a very well
 * specified mapping for the new version 5 encryption.</p>
 *
 * <p>So, for passwords specified in versions up to and including 4, a byte[]
 * representation is the most accurate, but not necessarily the most convenient
 * manner to provide passwords. For version 5, allowing passwords to be
 * specified as Strings will be the preferred mechanism. Rather than specify two
 * interfaces whenever a password can be provided - one for byte[] and one for
 * String - we express the password as a class. This class can also offer a best
 * guess at a String representation for a password for encryption versions up to
 * and including 4.</p>
 *
 * @author Luke Kirby
 */
public class PDFPassword {

    /** The empty password */
    public static final PDFPassword EMPTY_PASSWORD =
            new PDFPassword(new byte[0]);

    /**
     * Ensure a non-null PDFPassword by substituting the empty password
     * for a null password
     * @param password the password, may be null
     * @return a non-null password
     */
    public static PDFPassword nonNullPassword(PDFPassword password) {
        return password != null ? password : EMPTY_PASSWORD;
    }

    /** the password in bytes, if specified as such */
    private byte[] passwordBytes = null;
    /** the passwird as a string, if specified as such */
    private String passwordString = null;

    /**
     * Construct a byte-based password
     * @param passwordBytes the password bytes
     */
    public PDFPassword(byte[] passwordBytes) {
        this.passwordBytes =
                passwordBytes != null ? passwordBytes : new byte[0];
    }

    /**
     * Construct a string-based password
     * @param passwordString the password
     */
    public PDFPassword(String passwordString) {
        this.passwordString = passwordString != null ? passwordString : "";
    }

    /**
     * Get the password bytes.
     *
     * @param unicodeConversion whether the specific conversion from a unicode
     * String, as present for version 5 encryption, should be used
     * @return a list of possible password bytes
     */
    List<byte[]> getPasswordBytes(boolean unicodeConversion) {
        // TODO - handle unicodeConversion when we support version 5
        if (this.passwordBytes != null || this.passwordString == null) {
            return Collections.singletonList(this.passwordBytes);
        } else {
            if (isAlphaNum7BitString(this.passwordString)) {
                // there's no reasonthat this string would get encoded
                // in any other way
                return Collections.singletonList(
                        PDFStringUtil.asBytes(this.passwordString));
            } else {
                return generatePossiblePasswordBytes(this.passwordString);
            }
        }
    }

    /**
     * An array of password byte generators that attempts to enumerate the
     * possible strategies that an encrypting application might take to convert
     * a string to an array of bytes
     */
    private final static PasswordByteGenerator[] PASSWORD_BYTE_GENERATORS =
            new PasswordByteGenerator[]{

                    // The best option, and that recommended by the spec, is
                    // straight PDFDocEncoding of the string but its not
                    // mentioned what to do with undefined characters
                    // (presumably, an encryption generating app should not
                    // allow them, but there are no guarantees!). Plus, that
                    // hasn't always been the case. There's also a possiblity
                    // that we'll be presented with the byte encoding from
                    // whatever code page is default on the system that
                    // generated the password. I don't think we're going to try
                    // all different code pages, though. Here are
                    // a few ideas, anyway!

                    // skip undefined chars
                    new PDFDocEncodingByteGenerator(null),
                    // replace undefined chars with 0
                    new PDFDocEncodingByteGenerator(Byte.valueOf((byte) 0)),
                    // replace undefined chars with ?
                    new PDFDocEncodingByteGenerator(Byte.valueOf((byte) '?')),
                    // just strip the higher 8 bits!
                    new PasswordByteGenerator() {
                        @Override
						public byte[] generateBytes(String password) {
                            return PDFStringUtil.asBytes(password);
                        }
                    },
                    // skip 2-byte chars
                    new IdentityEncodingByteGenerator(null),
                    // replace 2-byte chars with 0
                    new IdentityEncodingByteGenerator(Byte.valueOf((byte) 0)),
                    // replace 2-byte chars with ?
                    new IdentityEncodingByteGenerator(Byte.valueOf((byte) '?'))
            };

    /**
     * Generate some possible byte representations of a string password
     *
     * @param passwordString the string password
     * @return a list of unique possible byte representations
     */
    private static List<byte[]> generatePossiblePasswordBytes(
            String passwordString) {

        final List<byte[]> possibilties = new ArrayList<byte[]>();
        for (final PasswordByteGenerator generator : PASSWORD_BYTE_GENERATORS) {
            byte[] generated = generator.generateBytes(passwordString);
            // avoid duplicates
            boolean alreadyGenerated = false;
            for (int i = 0; !alreadyGenerated && i < possibilties.size(); ++i) {
                if (Arrays.equals(possibilties.get(i), generated)) {
                    alreadyGenerated = true;
                }
            }
            if (!alreadyGenerated) {
                possibilties.add(generated);
            }
        }
        return possibilties;
    }

    private boolean isAlphaNum7BitString(String string) {
        for (int i = 0; i < string.length(); ++i) {
            final char c = string.charAt(i);
            if (c >= 127 || !Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a string password to a byte[] representation
     */
    private static interface PasswordByteGenerator {
        byte[] generateBytes(String password);
    }

    /**
     * Converts strings to byte by employing a {@link CharsetEncoder} and a
     * configurable mechanism to replace or ignore characters that are
     * unrepresentable according to the encoder.
     */
    private static abstract class CharsetEncoderGenerator
            implements PasswordByteGenerator {

        private Byte replacementByte;

        /**
         * Class constructor
         *
         * @param replacementByte the byte to replace to use to represent any
         * unrepresentable character, or null if unrepresentable characters
         * should just be ignored
         */
        protected CharsetEncoderGenerator(Byte replacementByte) {
            this.replacementByte = replacementByte;
        }


        @Override
		public byte[] generateBytes(String password) {
            final CharsetEncoder encoder = createCharsetEncoder();
            if (this.replacementByte != null) {
                encoder.replaceWith(new byte[]{this.replacementByte});
                encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            } else {
                encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            }
            try {
                final ByteBuffer b = encoder.encode(CharBuffer.wrap(password));
                final byte[] bytes = new byte[b.remaining()];
                b.get(bytes);
                return bytes;
            } catch (CharacterCodingException e) {
                // shouldn't happen: unmappable characters should be the only
                // problem, and we're not handling them with a report
                return null;
            }
        }

        protected abstract CharsetEncoder createCharsetEncoder();

    }

    /**
     * Generate byte[] representations based on the PDFDocEncoding
     */
    private static class PDFDocEncodingByteGenerator
            extends CharsetEncoderGenerator {

        private PDFDocEncodingByteGenerator(Byte replacementByte) {
            super(replacementByte);
        }

        @Override
		protected CharsetEncoder createCharsetEncoder() {
            return new PDFDocCharsetEncoder();
        }
    }

    /**
     * Generate byte[] representations based on a Unicode code point identity
     * encoding; characters over 255 in value are considered unrepresentable
     */
    private static class IdentityEncodingByteGenerator
            extends CharsetEncoderGenerator {

        private IdentityEncodingByteGenerator(Byte replacementByte) {
            super(replacementByte);
        }

        @Override
		protected CharsetEncoder createCharsetEncoder() {
            return new Identity8BitCharsetEncoder();
        }
    }

}