package org.openpdf.renderer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Configuration class.
 */
class ConfigurationTest {

    private Configuration config;

    @BeforeEach
    void setUp() {
        config = Configuration.getInstance();
    }

    @AfterEach
    void tearDown() {
        // Reset all configuration values to their defaults to ensure test isolation
        config.setConvertGreyscaleImagesToArgb(true);
        config.setThresholdForBandedImageRendering(0);
        config.setAvoidColorConvertOp(false);
        config.setUseBlurResizingForImages(true);
        config.setPrintSignatureFields(true);
        config.setPrintStampAnnotations(true);
        config.setPrintWidgetAnnotations(true);
        config.setPrintFreetextAnnotations(true);
        config.setPrintLinkAnnotations(true);
    }

    @Test
    void testGetInstance_ReturnsSingleton() {
        // when
        Configuration instance1 = Configuration.getInstance();
        Configuration instance2 = Configuration.getInstance();
        
        // then
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    void testConvertGreyscaleImagesToArgb_DefaultIsTrue() {
        // then
        assertThat(config.isConvertGreyscaleImagesToArgb()).isTrue();
    }

    @Test
    void testSetConvertGreyscaleImagesToArgb() {
        // when
        config.setConvertGreyscaleImagesToArgb(false);
        
        // then
        assertThat(config.isConvertGreyscaleImagesToArgb()).isFalse();
    }

    @Test
    void testThresholdForBandedImageRendering_DefaultIsZero() {
        // then
        assertThat(config.getThresholdForBandedImageRendering()).isZero();
    }

    @Test
    void testSetThresholdForBandedImageRendering() {
        // when
        config.setThresholdForBandedImageRendering(1000);
        
        // then
        assertThat(config.getThresholdForBandedImageRendering()).isEqualTo(1000);
    }

    @Test
    void testAvoidColorConvertOp_DefaultIsFalse() {
        // then
        assertThat(config.isAvoidColorConvertOp()).isFalse();
    }

    @Test
    void testSetAvoidColorConvertOp() {
        // when
        config.setAvoidColorConvertOp(true);
        
        // then
        assertThat(config.isAvoidColorConvertOp()).isTrue();
    }

    @Test
    void testUseBlurResizingForImages_DefaultIsTrue() {
        // then
        assertThat(config.isUseBlurResizingForImages()).isTrue();
    }

    @Test
    void testSetUseBlurResizingForImages() {
        // when
        config.setUseBlurResizingForImages(false);
        
        // then
        assertThat(config.isUseBlurResizingForImages()).isFalse();
    }

    @Test
    void testPrintSignatureFields_DefaultIsTrue() {
        // then
        assertThat(config.isPrintSignatureFields()).isTrue();
    }

    @Test
    void testSetPrintSignatureFields() {
        // when
        config.setPrintSignatureFields(false);
        
        // then
        assertThat(config.isPrintSignatureFields()).isFalse();
    }

    @Test
    void testPrintStampAnnotations_DefaultIsTrue() {
        // then
        assertThat(config.isPrintStampAnnotations()).isTrue();
    }

    @Test
    void testSetPrintStampAnnotations() {
        // when
        config.setPrintStampAnnotations(false);
        
        // then
        assertThat(config.isPrintStampAnnotations()).isFalse();
    }

    @Test
    void testPrintWidgetAnnotations_DefaultIsTrue() {
        // then
        assertThat(config.isPrintWidgetAnnotations()).isTrue();
    }

    @Test
    void testSetPrintWidgetAnnotations() {
        // when
        config.setPrintWidgetAnnotations(false);
        
        // then
        assertThat(config.isPrintWidgetAnnotations()).isFalse();
    }

    @Test
    void testPrintFreetextAnnotations_DefaultIsTrue() {
        // then
        assertThat(config.isPrintFreetextAnnotations()).isTrue();
    }

    @Test
    void testSetPrintFreetextAnnotations() {
        // when
        config.setPrintFreetextAnnotations(false);
        
        // then
        assertThat(config.isPrintFreetextAnnotations()).isFalse();
    }

    @Test
    void testPrintLinkAnnotations_DefaultIsTrue() {
        // then
        assertThat(config.isPrintLinkAnnotations()).isTrue();
    }

    @Test
    void testSetPrintLinkAnnotations() {
        // when
        config.setPrintLinkAnnotations(false);
        
        // then
        assertThat(config.isPrintLinkAnnotations()).isFalse();
    }

    @Test
    void testMultipleConfigurationChanges() {
        // when - change multiple settings
        config.setConvertGreyscaleImagesToArgb(false);
        config.setThresholdForBandedImageRendering(500);
        config.setAvoidColorConvertOp(true);
        config.setPrintSignatureFields(false);
        
        // then - all changes are reflected
        assertThat(config.isConvertGreyscaleImagesToArgb()).isFalse();
        assertThat(config.getThresholdForBandedImageRendering()).isEqualTo(500);
        assertThat(config.isAvoidColorConvertOp()).isTrue();
        assertThat(config.isPrintSignatureFields()).isFalse();
    }
}
