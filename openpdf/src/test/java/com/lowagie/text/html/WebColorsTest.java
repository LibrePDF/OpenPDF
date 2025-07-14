package com.lowagie.text.html;

import java.awt.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebColorsTest {

    public WebColorsTest() {
        super();
    }

    @Test
    public void testNullName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            WebColors.getRGBColor(null);
        });
    }

    @Test
    public void testEmptyName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            WebColors.getRGBColor("");
        });
    }

    @Test
    public void testBlankName() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            WebColors.getRGBColor(" ");
        });
    }

    @Test
    public void testTransparency() {
        // Test for PR #378
        Color color = WebColors.getRGBColor("black");
        Color expected = new Color(0, 0, 0, 255);
        Assertions.assertEquals(expected, color);
    }


    @Test
    public void testHexadecimalSintax() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("#f09"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("#F09"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("#ff0099"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("#FF0099"));
    }

    @Test
    public void testHexadecimalSintaxWithAlpha() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xaa);
        // Hexadecimal syntax with alpha value 
        Assertions.assertEquals(expected, WebColors.getRGBColor("#f09a"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("#F09A"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("#ff0099aa"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("#FF0099aa"));
    }

    @Test
    public void testHexadecimalBadFormats() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            WebColors.getRGBColor("#");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            WebColors.getRGBColor("#ab");
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            WebColors.getRGBColor("#zzz");
        });
    }

    @Test
    public void testRGB() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255,0,153)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255, 0, 153)"));
    }

    @Test
    public void testRGBPercentage() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(100%,0%,60%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(100%, 0%, 60%)"));
    }

    @Test
    public void testRGBSpaces() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255 0 153)"));
    }

    @Test
    public void testRGBAlpha() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255, 0, 153, 1)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255, 0, 153, 100%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255 0 153 / 1)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255 0 153 / 100%)"));

        Color expected2 = new Color(0xff, 0x00, 0x99, 0x99);
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgb(255, 0, 153, 0.6)"));
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgb(255, 0, 153, 60%)"));
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgb(255 0 153 / 0.6)"));
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgb(255 0 153 / 60%)"));
    }

    @Test
    public void testRGBAAlias() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgba(255, 0, 153, 1)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgba(255, 0, 153, 100%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgba(255 0 153 / 1)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgba(255 0 153 / 100%)"));

        Color expected2 = new Color(0xff, 0x00, 0x99, 0x99);
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgba(255, 0, 153, 0.6)"));
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgba(255, 0, 153, 60%)"));
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgba(255 0 153 / 0.6)"));
        Assertions.assertEquals(expected2, WebColors.getRGBColor("rgba(255 0 153 / 60%)"));
    }

    @Test
    public void testRGBFloatValues() {
        Color expected = new Color(0xff, 0x00, 0x99, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255, 0, 153.0)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(255, 0, 153.6, 1)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("rgb(2.55e2, .1e0, 1.53e2, +.1e3%)"));
    }

    @Test
    public void testHSL() {
        Color expected = new Color(0xb3, 0x85, 0xe1, 0xff);
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270,60%,70%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270, 60%, 70%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270 60% 70%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270deg, 60%, 70%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(4.71239rad, 60%, 70%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(.75turn, 60%, 70%)"));
    }

    @Test
    public void testHSLA() {
        Color expected = new Color(0xb3, 0x85, 0xe1, (int) (255 * 0.15));

        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270, 60%, 70%, .15)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270, 60%, 70%, 15%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270 60% 70% / .15)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsl(270 60% 70% / 15%)"));


    }

    @Test
    public void testHSLAAlias() {
        Color expected = new Color(0xb3, 0x85, 0xe1, (int) (255 * 0.15));

        Assertions.assertEquals(expected, WebColors.getRGBColor("hsla(270, 60%, 70%, .15)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsla(270, 60%, 70%, 15%)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsla(270 60% 70% / .15)"));
        Assertions.assertEquals(expected, WebColors.getRGBColor("hsla(270 60% 70% / 15%)"));


    }


}
