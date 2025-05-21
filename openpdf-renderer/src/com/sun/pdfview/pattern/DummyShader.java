package com.sun.pdfview.pattern;

import java.awt.Color;
import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;

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
