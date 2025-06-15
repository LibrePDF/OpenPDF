package org.openpdf.renderer.pattern;

import java.awt.Color;
import java.io.IOException;

import org.openpdf.renderer.PDFObject;
import org.openpdf.renderer.PDFPaint;

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
