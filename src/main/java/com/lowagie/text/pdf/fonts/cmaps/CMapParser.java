/**
 * Copyright (c) 2005-2006, www.fontbox.org
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import com.lowagie.text.error_messages.MessageLocalization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This will parser a CMap stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 4065 $
 * @since	2.1.4
 */
public class CMapParser
{
    private static final String BEGIN_CODESPACE_RANGE = "begincodespacerange";
    private static final String BEGIN_BASE_FONT_CHAR = "beginbfchar";
    private static final String BEGIN_BASE_FONT_RANGE = "beginbfrange";
    
    private static final String MARK_END_OF_DICTIONARY = ">>";
    private static final String MARK_END_OF_ARRAY = "]";
    
    private byte[] tokenParserByteBuffer = new byte[512];

    /**
     * Creates a new instance of CMapParser.
     */
    public CMapParser()
    {
    }

    /**
     * This will parse the stream and create a cmap object.
     *
     * @param input The CMAP stream to parse.
     * @return The parsed stream as a java object.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    public CMap parse( InputStream input ) throws IOException
    {
        PushbackInputStream cmapStream = new PushbackInputStream( input );
        CMap result = new CMap();
        Object previousToken = null;
        Object token = null;
        while( (token = parseNextToken( cmapStream )) != null )
        {
            if( token instanceof Operator )
            {
                Operator op = (Operator)token;
                if( op.op.equals( BEGIN_CODESPACE_RANGE ) )
                {
                    Number cosCount = (Number)previousToken;
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] startRange = (byte[])parseNextToken( cmapStream );
                        byte[] endRange = (byte[])parseNextToken( cmapStream );
                        CodespaceRange range = new CodespaceRange();
                        range.setStart( startRange );
                        range.setEnd( endRange );
                        result.addCodespaceRange( range );
                    }
                }
                else if( op.op.equals( BEGIN_BASE_FONT_CHAR ) )
                {
                    Number cosCount = (Number)previousToken;
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] inputCode = (byte[])parseNextToken( cmapStream );
                        Object nextToken = parseNextToken( cmapStream );
                        if( nextToken instanceof byte[] )
                        {
                            byte[] bytes = (byte[])nextToken;
                            String value = createStringFromBytes( bytes );
                            result.addMapping( inputCode, value );
                        }
                        else if( nextToken instanceof LiteralName )
                        {
                            result.addMapping( inputCode, ((LiteralName)nextToken).name );
                        }
                        else
                        {
                            throw new IOException(MessageLocalization.getComposedMessage("error.parsing.cmap.beginbfchar.expected.cosstring.or.cosname.and.not.1", nextToken));
                        }
                    }
                }
               else if( op.op.equals( BEGIN_BASE_FONT_RANGE ) )
                {
                    Number cosCount = (Number)previousToken;
                    
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] startCode = (byte[])parseNextToken( cmapStream );
                        byte[] endCode = (byte[])parseNextToken( cmapStream );
                        Object nextToken = parseNextToken( cmapStream );
                        List array = null;
                        byte[] tokenBytes = null;
                        if( nextToken instanceof List )
                        {
                            array = (List)nextToken;
                            tokenBytes = (byte[])array.get( 0 );
                        }
                        else
                        {
                            tokenBytes = (byte[])nextToken;
                        }
                        
                        String value = null;
                        
                        int arrayIndex = 0;
                        boolean done = false;
                        while( !done )
                        {
                            if( compare( startCode, endCode ) >= 0 )
                            {
                                done = true;
                            }
                            value = createStringFromBytes( tokenBytes );
                            result.addMapping( startCode, value );
                            increment( startCode );
                            
                            if( array == null )
                            {
                                increment( tokenBytes );
                            }
                            else
                            {
                                arrayIndex++;
                                if( arrayIndex < array.size() )
                                {
                                    tokenBytes = (byte[])array.get( arrayIndex );
                                }
                            }
                        }
                    }
                }
            }
            previousToken = token;
        }
        return result;
    }
    
    private Object parseNextToken( PushbackInputStream is ) throws IOException
    {
        Object retval = null;
        int nextByte = is.read();
        //skip whitespace
        while( nextByte == 0x09 || nextByte == 0x20 || nextByte == 0x0D || nextByte == 0x0A )
        {
            nextByte = is.read();
        }
        switch( nextByte )
        {
            case '%':
            {
                //header operations, for now return the entire line 
                //may need to smarter in the future
                StringBuffer buffer = new StringBuffer();
                buffer.append( (char)nextByte );
                readUntilEndOfLine( is, buffer );
                retval = buffer.toString();
                break;
            }
            case '(':
            {
                StringBuffer buffer = new StringBuffer();
                int stringByte = is.read();
                
                while( stringByte != -1 && stringByte != ')' )
                {
                    buffer.append( (char)stringByte );
                    stringByte = is.read();
                }
                retval = buffer.toString();
                break;
            }
            case '>':
            {
                int secondCloseBrace = is.read();
                if( secondCloseBrace == '>' )
                {
                    retval = MARK_END_OF_DICTIONARY;
                }
                else
                {
                    throw new IOException(MessageLocalization.getComposedMessage("error.expected.the.end.of.a.dictionary"));
                }
                break;
            }
            case ']':
            {
                retval = MARK_END_OF_ARRAY;
                break;
            }
            case '[':
            {
                List list = new ArrayList();
                
                Object nextToken = parseNextToken( is ); 
                while( nextToken != MARK_END_OF_ARRAY )
                {
                    list.add( nextToken );
                    nextToken = parseNextToken( is );
                }
                retval = list;
                break;
            }
            case '<':
            {
                int theNextByte = is.read();
                if( theNextByte == '<' )
                {
                    Map result = new HashMap();
                    //we are reading a dictionary
                    Object key = parseNextToken( is ); 
                    while( key instanceof LiteralName && key != MARK_END_OF_DICTIONARY )
                    {
                        Object value = parseNextToken( is );
                        result.put( ((LiteralName)key).name, value );
                        key = parseNextToken( is );
                    }
                    retval = result;
                }
                else
                {
                    //won't read more than 512 bytes
                    
                    int multiplyer = 16;
                    int bufferIndex = -1;
                    while( theNextByte != -1 && theNextByte != '>' )
                    {
                        int intValue = 0;
                        if( theNextByte >= '0' && theNextByte <= '9' )
                        {
                            intValue = theNextByte - '0';
                        }
                        else if( theNextByte >= 'A' && theNextByte <= 'F' )
                        {
                            intValue = 10 + theNextByte - 'A';
                        }
                        else if( theNextByte >= 'a' && theNextByte <= 'f' )
                        {
                            intValue = 10 + theNextByte - 'a';
                        }
                        else
                        {
                            throw new IOException(MessageLocalization.getComposedMessage("error.expected.hex.character.and.not.char.thenextbyte.1", theNextByte));
                        }
                        intValue *= multiplyer;
                        if( multiplyer == 16 )
                        {
                            bufferIndex++;
                            tokenParserByteBuffer[bufferIndex] = 0;
                            multiplyer = 1;
                        }
                        else
                        {
                            multiplyer = 16;
                        }
                        tokenParserByteBuffer[bufferIndex]+= intValue;
                        theNextByte = is.read();
                    }
                    byte[] finalResult = new byte[bufferIndex+1];
                    System.arraycopy(tokenParserByteBuffer,0,finalResult, 0, bufferIndex+1);
                    retval = finalResult;
                }
                break;
            }
            case '/':
            {
                StringBuffer buffer = new StringBuffer();
                int stringByte = is.read();
                
                while( !isWhitespaceOrEOF( stringByte ) )
                {
                    buffer.append( (char)stringByte );
                    stringByte = is.read();
                }
                retval = new LiteralName( buffer.toString() );
                break;
            }
            case -1:
            {
                //EOF return null;
                break;
            }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append( (char)nextByte );
                nextByte = is.read();
                
                while( !isWhitespaceOrEOF( nextByte ) &&
                        (Character.isDigit( (char)nextByte )||
                         nextByte == '.' ) )
                {
                    buffer.append( (char)nextByte );
                    nextByte = is.read();
                }
                is.unread( nextByte );
                String value = buffer.toString();
                if( value.indexOf( '.' ) >=0 )
                {
                    retval = new Double( value );
                }
                else
                {
                    retval = new Integer( buffer.toString() );
                }
                break;
            }
            default:
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append( (char)nextByte );
                nextByte = is.read();
                
                while( !isWhitespaceOrEOF( nextByte ) )
                {
                    buffer.append( (char)nextByte );
                    nextByte = is.read();
                }
                retval = new Operator( buffer.toString() );                        
                
                break;
            }
        }
        return retval;
    }
    
    private void readUntilEndOfLine( InputStream is, StringBuffer buf ) throws IOException
    {
        int nextByte = is.read();
        while( nextByte != -1 && nextByte != 0x0D && nextByte != 0x0A )
        {
            buf.append( (char)nextByte );
            nextByte = is.read();
        }
    }
    
    private boolean isWhitespaceOrEOF( int aByte )
    {
        return aByte == -1 || aByte == 0x20 || aByte == 0x0D || aByte == 0x0A; 
    }
    

    private void increment( byte[] data )
    {
        increment( data, data.length-1 );
    }

    private void increment( byte[] data, int position )
    {
        if( position > 0 && (data[position]+256)%256 == 255 )
        {
            data[position]=0;
            increment( data, position-1);
        }
        else
        {
            data[position] = (byte)(data[position]+1);
        }
    }
    
    private String createStringFromBytes( byte[] bytes ) throws IOException
    {
        String retval = null;
        if( bytes.length == 1 )
        {
            retval = new String( bytes );
        }
        else
        {
            retval = new String( bytes, "UTF-16BE" );
        }
        return retval;
    }

    private int compare( byte[] first, byte[] second )
    {
        int retval = 1;
        boolean done = false;
        for( int i=0; i<first.length && !done; i++ )
        {
            if( first[i] == second[i] )
            {
                //move to next position
            }
            else if( ((first[i]+256)%256) < ((second[i]+256)%256) )
            {
                done = true;
                retval = -1;
            }
            else
            {
                done = true;
                retval = 1;
            }
        }
        return retval;
    }
    
    /**
     * Internal class.
     */
    private class LiteralName
    {
        private String name;
        private LiteralName( String theName )
        {
            name = theName;
        }
    }
    
    /**
     * Internal class.
     */
    private class Operator
    {
        private String op;
        private Operator( String theOp )
        {
            op = theOp;
        }
    }
    
    /**
     * A simple class to test parsing of cmap files.
     * 
     * @param args Some command line arguments.
     * 
     * @throws Exception If there is an error parsing the file.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            System.err.println( "usage: java org.pdfbox.cmapparser.CMapParser <CMAP File>" );
            System.exit( -1 );
        }
        CMapParser parser = new CMapParser(  );
        CMap result = parser.parse( new FileInputStream( args[0] ) );
        System.out.println( "Result:" + result );
    }
}
