/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package com.lowagie.text.pdf.fonts.cmaps;

/**
 * This represents a single entry in the codespace range.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 3646 $
 * @since	2.1.4
 */
public class CodespaceRange
{

    private byte[] start;
    private byte[] end;

    /**
     * Creates a new instance of CodespaceRange.
     */
    public CodespaceRange()
    {
    }

    /** Getter for property end.
     * @return Value of property end.
     *
     */
    public byte[] getEnd()
    {
        return this.end;
    }

    /** Setter for property end.
     * @param endBytes New value of property end.
     *
     */
    public void setEnd(byte[] endBytes)
    {
        end = endBytes;
    }

    /** Getter for property start.
     * @return Value of property start.
     *
     */
    public byte[] getStart()
    {
        return this.start;
    }

    /** Setter for property start.
     * @param startBytes New value of property start.
     *
     */
    public void setStart(byte[] startBytes)
    {
        start = startBytes;
    }

}