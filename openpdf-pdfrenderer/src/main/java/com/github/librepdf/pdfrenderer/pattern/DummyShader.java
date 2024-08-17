package com.github.librepdf.pdfrenderer.pattern;

import java.awt.Color;
import java.io.IOException;

import com.github.librepdf.pdfrenderer.PDFObject;
import com.github.librepdf.pdfrenderer.PDFPaint;

public class DummyShader extends PDFShader {

	protected DummyShader(int type) {
		super(type);
	}

	@Override
	public void parse(PDFObject shareObj) throws IOException {
		
	}

	@Override
	public PDFPaint getPaint() {
		return PDFPaint.getPaint(Color.PINK);
	}
		
}
