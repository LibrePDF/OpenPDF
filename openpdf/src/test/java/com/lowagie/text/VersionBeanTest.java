package com.lowagie.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VersionBeanTest {

    private VersionBean versionBean;
    private Manifest manifest;

    @BeforeEach
    void beforeAll() {
        versionBean = new VersionBean();
        manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putValue("Implementation-Vendor", "Vendor Impl");
        mainAttributes.putValue("Implementation-Title", "Title-Impl");
        mainAttributes.putValue("SCM-Timestamp", "SCM-TS");
        mainAttributes.putValue("Bundle-Version", "Bundle-Ver");
        mainAttributes.putValue("Implementation-Version", "Version Impl");
        versionBean.setManifest(manifest);
    }

    @Test
    void getVendor() {
        String vendor = versionBean.getVendor();
        assertEquals("Vendor Impl", vendor);
    }

    @Test
    void getTitle() {
        String title = versionBean.getTitle();
        assertEquals("Title-Impl", title);
    }

    @Test
    void getTimestamp() {
        String timestamp = versionBean.getTimestamp();
        assertEquals("SCM-TS", timestamp);
    }

    @Test
    void getVersion() {
        VersionBean.Version version = versionBean.getVersion();
        assertSame(VersionBean.VERSION, version);
    }

    @Test
    void testVersionVersionFallBack() {
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.putValue("Implementation-Version", null);
        versionBean.setManifest(manifest);

        VersionBean.Version version = VersionBean.VERSION;
        assertEquals("Bundle-Ver", version.getVersion());
        assertEquals("Bundle-Ver", version.getImplementationVersion());
    }

    @Test
    void testVersionVersion() {
        VersionBean.Version version = VersionBean.VERSION;
        assertEquals("Version Impl", version.getVersion());
        assertEquals("Version Impl", version.getImplementationVersion());
    }

    @Test
    void testToString() {
        assertEquals("Title-Impl by Vendor Impl, version Version Impl", versionBean.toString());
    }

}