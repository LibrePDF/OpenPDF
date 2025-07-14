package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class PdfStructureTreeRootTest {

    @Test
    void shouldCreateNewInstanceSuccessfully() {
        PdfWriter writer = mock(PdfWriter.class);
        when(writer.getPdfIndirectReference()).thenReturn(mock(PdfIndirectReference.class));

        PdfStructureTreeRoot root = new PdfStructureTreeRoot(writer);

        assertNotNull(root);
        assertEquals(PdfName.STRUCTTREEROOT, root.get(PdfName.TYPE));
        assertNotNull(root.getReference());
        assertSame(writer, root.getWriter());
    }

    @Test
    void shouldMapUserTagToStandardTag() {
        PdfStructureTreeRoot root = new PdfStructureTreeRoot(mock(PdfWriter.class));
        PdfName userTag = new PdfName("MyTag");
        PdfName standardTag = PdfName.H1;

        root.mapRole(userTag, standardTag);

        PdfDictionary roleMap = (PdfDictionary) root.get(PdfName.ROLEMAP);
        assertNotNull(roleMap);
        assertEquals(standardTag, roleMap.get(userTag));
    }

    @Test
    void getWriterShouldReturnCorrectWriter() {
        PdfWriter mockWriter = mock(PdfWriter.class);
        PdfStructureTreeRoot treeRoot = new PdfStructureTreeRoot(mockWriter);

        PdfWriter result = treeRoot.getWriter();

        assertSame(mockWriter, result);
    }

    @Test
    void addExistingObjectShouldIncreaseParentTreeNextKey() {
        PdfWriter mockWriter = mock(PdfWriter.class);
        PdfStructureTreeRoot treeRoot = new PdfStructureTreeRoot(mockWriter);
        PdfIndirectReference mockRef = mock(PdfIndirectReference.class);

        int firstKey = treeRoot.addExistingObject(mockRef);
        int secondKey = treeRoot.addExistingObject(mockRef);

        assertNotEquals(firstKey, secondKey);
        assertEquals(firstKey + 1, secondKey);
    }

    @Test
    void buildTreeShouldGenerateParentTreeWithoutException() throws IOException {
        PdfWriter mockWriter = mock(PdfWriter.class);
        when(mockWriter.addToBody(any(PdfObject.class))).thenAnswer(invocation -> {
            PdfObject arg = invocation.getArgument(0);
            return new PdfIndirectObject(0, arg, mockWriter);
        });

        PdfStructureTreeRoot treeRoot = new PdfStructureTreeRoot(mockWriter);

        assertDoesNotThrow(treeRoot::buildTree);
    }

    @Test
    void buildTreeShouldHandleIOException() throws IOException {
        PdfWriter mockWriter = mock(PdfWriter.class);
        doThrow(IOException.class).when(mockWriter).addToBody(any(PdfObject.class));

        PdfStructureTreeRoot treeRoot = new PdfStructureTreeRoot(mockWriter);
        treeRoot.setPageMark(1, mock(PdfIndirectReference.class));

        assertThrows(IOException.class, treeRoot::buildTree);
    }

    @Test
    void getOrCreatePageKeyShouldCreateNewPageArrayWhenNotExists() {
        PdfWriter mockWriter = mock(PdfWriter.class);
        PdfStructureTreeRoot treeRoot = new PdfStructureTreeRoot(mockWriter);

        PdfIndirectReference mockRef = mock(PdfIndirectReference.class);

        int firstKey = treeRoot.addExistingObject(mockRef);
        assertEquals(0, firstKey);

        int pageKey = treeRoot.getOrCreatePageKey(1);
        assertEquals(1, pageKey);
    }

    @Test
    void getOrCreatePageKeyShouldReturnExistingPageKey() {
        PdfWriter mockWriter = mock(PdfWriter.class);
        PdfStructureTreeRoot treeRoot = new PdfStructureTreeRoot(mockWriter);

        PdfIndirectReference mockRef = mock(PdfIndirectReference.class);

        int firstKey = treeRoot.addExistingObject(mockRef);
        assertEquals(0, firstKey);

        //key should be created when setting page mark
        treeRoot.setPageMark(1, mock(PdfIndirectReference.class));

        //existing key should be returned
        int pageKey = treeRoot.getOrCreatePageKey(1);
        assertEquals(1, pageKey);
    }
}