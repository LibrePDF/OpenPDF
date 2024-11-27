package com.lowagie.text;

public class WMFData {
    public final float scaledWidth;
    public final float scaledHeight;
    public final float dpiX;
    public final float dpiY;

    public WMFData(float scaledWidth, float scaledHeight, float dpiX, float dpiY) {
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
        this.dpiX = dpiX;
        this.dpiY = dpiY;
    }
}
