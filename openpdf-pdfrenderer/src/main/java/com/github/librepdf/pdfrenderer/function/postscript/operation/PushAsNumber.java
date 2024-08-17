package com.github.librepdf.pdfrenderer.function.postscript.operation;

import java.util.Stack;



final class PushAsNumber implements PostScriptOperation {

	private String token;
	
	/*************************************************************************
	 * Constructor
	 * @param numberToken
	 ************************************************************************/
	
	public PushAsNumber(String numberToken) {
		super();
		this.token = numberToken;
	}
	
	/*************************************************************************
	 * eval
	 * @see PostScriptOperation#eval(java.util.Stack)
	 ************************************************************************/
	@Override
	public void eval(Stack<Object> environment) {
		try {
			double number = Double.parseDouble(this.token);
			environment.push(number);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("PS token is not supported "+this.token); 
		}	}

}

