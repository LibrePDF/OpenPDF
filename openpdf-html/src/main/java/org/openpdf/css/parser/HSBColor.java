package org.openpdf.css.parser;

import java.util.Objects;

public final class HSBColor {
    private final float hue;
    private final float saturation;
    private final float brightness;

    public HSBColor(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public float hue() { return hue; }
    public float saturation() { return saturation; }
    public float brightness() { return brightness; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HSBColor that)) return false;
        return Float.compare(hue, that.hue) == 0
                && Float.compare(saturation, that.saturation) == 0
                && Float.compare(brightness, that.brightness) == 0;
    }

    @Override
    public int hashCode() { return Objects.hash(hue, saturation, brightness); }

    @Override
    public String toString() {
        return "HSBColor[hue=" + hue + ", saturation=" + saturation + ", brightness=" + brightness + "]";
    }

    // Taken from java.awt.Color to avoid dependency on it
    public FSRGBColor toRGB() {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return new FSRGBColor(r, g, b);
    }
}
