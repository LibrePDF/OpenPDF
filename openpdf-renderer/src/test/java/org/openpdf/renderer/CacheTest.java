package org.openpdf.renderer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;

/**
 * Unit tests for Cache class.
 */
class CacheTest {

    private Cache cache;

    @BeforeEach
    void setUp() {
        cache = new Cache();
    }

    @Test
    void testAddAndGetPage() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        
        // when
        cache.addPage(pageNumber, mockPage);
        PDFPage result = cache.getPage(pageNumber);
        
        // then
        assertThat(result).isSameAs(mockPage);
    }

    @Test
    void testGetPage_NotInCache_ReturnsNull() {
        // when
        PDFPage result = cache.getPage(999);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testAddPageWithParser() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        PDFParser mockParser = mock(PDFParser.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        
        // when
        cache.addPage(pageNumber, mockPage, mockParser);
        PDFPage resultPage = cache.getPage(pageNumber);
        PDFParser resultParser = cache.getPageParser(pageNumber);
        
        // then
        assertThat(resultPage).isSameAs(mockPage);
        assertThat(resultParser).isSameAs(mockParser);
    }

    @Test
    void testGetPageParser_NotInCache_ReturnsNull() {
        // when
        PDFParser result = cache.getPageParser(999);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testRemovePage() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        cache.addPage(pageNumber, mockPage);
        
        // when
        cache.removePage(pageNumber);
        PDFPage result = cache.getPage(pageNumber);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testAddAndGetImage() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        ImageInfo mockInfo = mock(ImageInfo.class);
        BufferedImage mockImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        
        // when
        cache.addImage(mockPage, mockInfo, mockImage);
        BufferedImage result = cache.getImage(mockPage, mockInfo);
        
        // then
        assertThat(result).isSameAs(mockImage);
    }

    @Test
    void testGetImage_NotInCache_ReturnsNull() {
        // given
        PDFPage mockPage = mock(PDFPage.class);
        ImageInfo mockInfo = mock(ImageInfo.class);
        when(mockPage.getPageNumber()).thenReturn(1);
        
        // when
        BufferedImage result = cache.getImage(mockPage, mockInfo);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testAddImageWithRenderer() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        ImageInfo mockInfo = mock(ImageInfo.class);
        BufferedImage mockImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        PDFRenderer mockRenderer = mock(PDFRenderer.class);
        
        // when
        cache.addImage(mockPage, mockInfo, mockImage, mockRenderer);
        BufferedImage resultImage = cache.getImage(mockPage, mockInfo);
        PDFRenderer resultRenderer = cache.getImageRenderer(mockPage, mockInfo);
        
        // then
        assertThat(resultImage).isSameAs(mockImage);
        assertThat(resultRenderer).isSameAs(mockRenderer);
    }

    @Test
    void testGetImageRenderer_NotInCache_ReturnsNull() {
        // given
        PDFPage mockPage = mock(PDFPage.class);
        ImageInfo mockInfo = mock(ImageInfo.class);
        when(mockPage.getPageNumber()).thenReturn(1);
        
        // when
        PDFRenderer result = cache.getImageRenderer(mockPage, mockInfo);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testRemoveImage() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        ImageInfo mockInfo = mock(ImageInfo.class);
        BufferedImage mockImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        cache.addImage(mockPage, mockInfo, mockImage);
        
        // when
        cache.removeImage(mockPage, mockInfo);
        BufferedImage result = cache.getImage(mockPage, mockInfo);
        
        // then
        assertThat(result).isNull();
    }

    @Test
    void testMultiplePages() {
        // given
        PDFPage mockPage1 = mock(PDFPage.class);
        PDFPage mockPage2 = mock(PDFPage.class);
        when(mockPage1.getPageNumber()).thenReturn(1);
        when(mockPage2.getPageNumber()).thenReturn(2);
        
        // when
        cache.addPage(1, mockPage1);
        cache.addPage(2, mockPage2);
        
        // then
        assertThat(cache.getPage(1)).isSameAs(mockPage1);
        assertThat(cache.getPage(2)).isSameAs(mockPage2);
    }

    @Test
    void testMultipleImagesOnSamePage() {
        // given
        Integer pageNumber = 1;
        PDFPage mockPage = mock(PDFPage.class);
        when(mockPage.getPageNumber()).thenReturn(pageNumber);
        ImageInfo mockInfo1 = mock(ImageInfo.class);
        ImageInfo mockInfo2 = mock(ImageInfo.class);
        BufferedImage mockImage1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage mockImage2 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        
        // when
        cache.addImage(mockPage, mockInfo1, mockImage1);
        cache.addImage(mockPage, mockInfo2, mockImage2);
        
        // then
        assertThat(cache.getImage(mockPage, mockInfo1)).isSameAs(mockImage1);
        assertThat(cache.getImage(mockPage, mockInfo2)).isSameAs(mockImage2);
    }
}
