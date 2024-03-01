package com.lowagie.text.pdf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class LZWDecoderTest {

    private LZWDecoder lzwDecoder;
    private ByteArrayOutputStream outputStreamMock;

    @Before
    public void setUp() {
        lzwDecoder = new LZWDecoder();
        outputStreamMock = Mockito.mock(ByteArrayOutputStream.class);
    }

    @Test
    public void testInitializeStringTable() {
        lzwDecoder.initializeStringTable();
        assertNotNull(lzwDecoder.stringTable);
        assertEquals(8192, lzwDecoder.stringTable.length);
        for (int i = 0; i < 256; i++) {
            assertNotNull(lzwDecoder.stringTable[i]);
            assertEquals(1, lzwDecoder.stringTable[i].length);
            assertEquals((byte) i, lzwDecoder.stringTable[i][0]);
        }
        assertEquals(258, lzwDecoder.tableIndex);
        assertEquals(9, lzwDecoder.bitsToGet);
    }


    @Test
    public void testComposeString() {
        byte[] oldString = {1, 2};
        byte newByte = 3;
        byte[] result = lzwDecoder.composeString(oldString, newByte);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

}

