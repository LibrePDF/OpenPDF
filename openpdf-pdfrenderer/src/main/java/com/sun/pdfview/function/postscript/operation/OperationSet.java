package com.sun.pdfview.function.postscript.operation;

import java.util.HashMap;
import java.util.Map;

public class OperationSet {
	
    /** the set of all Operations we support. These operations are defined
     * in Appendix B - Operators.*/
    private Map<String, PostScriptOperation> operationSet = null;
    
    private static OperationSet instance;
    
    /*************************************************************************
     * Constructor
     ************************************************************************/
    
    private OperationSet() {
    	super();
    	initOperations();
    }
    
    /*************************************************************************
     * @return
     ************************************************************************/
    
    public static synchronized OperationSet getInstance() {
    	if (instance == null) {
    		instance = new OperationSet();
    	}
    	return instance;
    }
    
    /*************************************************************************
     * @param token
     * @return
     ************************************************************************/
    
    public PostScriptOperation getOperation(String token) {
    	PostScriptOperation result = this.operationSet.get(token.trim().toLowerCase());
    	if (result == null) {
    		result = new PushAsNumber(token);
    	}
    	return result;
    	
    }
    
    
    /**
     * Initialize the operations that we can perform.
     */
    private void initOperations() {
        /** these operators consider the left hand arguments as deeper in
         * the stack than the right hand arguments, thus the right-hand is
         * is the top of the stack and is popped first.
         *
         * PostScriptOperation details in PostScript Language Reference Manual:
         * http://www.adobe.com/products/postscript/pdfs/PLRM.pdf
         * Chapter 8 - Operator Details
         */
        if (this.operationSet == null) {
            this.operationSet = new HashMap<String, PostScriptOperation>();
           
            // Arithmetic Operators
            this.operationSet.put("abs", new Abs());
            this.operationSet.put("add", new Add());
            this.operationSet.put("atan", new Atan());
            this.operationSet.put("ceiling", new Ceiling());
            this.operationSet.put("cvi", new Cvi());
            this.operationSet.put("cvr", new Cvr());
            this.operationSet.put("div", new Div());
            this.operationSet.put("exp", new Exp());
            this.operationSet.put("floor", new Floor());
            this.operationSet.put("idiv", new Idiv());
            this.operationSet.put("ln", new Ln());
            this.operationSet.put("log", new Log());
            this.operationSet.put("mod", new Mod());
            this.operationSet.put("mul", new Mul());
            this.operationSet.put("neg", new Neg());
            this.operationSet.put("round", new Round());
            this.operationSet.put("sin", new Sin());
            this.operationSet.put("sqrt", new Sqrt());
            this.operationSet.put("sub", new Sub());
            this.operationSet.put("truncate", new Truncate());

            // Relational, boolean, and bitwise operators
            this.operationSet.put("and", new And());
            this.operationSet.put("bitshift", new Bitshift());
            this.operationSet.put("eq", new Eq());
            this.operationSet.put("false", new False());
            this.operationSet.put("ge", new Ge());
            this.operationSet.put("gt",new Gt());
            this.operationSet.put("le", new Le());
            this.operationSet.put("lt", new Lt());
            this.operationSet.put("ne", new Ne());
            this.operationSet.put("not", new Not());
            this.operationSet.put("or", new Or());
            this.operationSet.put("true", new True());
            this.operationSet.put("xor", new Xor());

            // Conditional Operators
            this.operationSet.put("if", new If());
            this.operationSet.put("ifelse", new IfElse());

            // Stack Operators
            this.operationSet.put("copy", new Copy());
            this.operationSet.put("dup", new Dup());
            this.operationSet.put("exch", new Exch());
            this.operationSet.put("index", new Index());
            this.operationSet.put("pop", new Pop());
            this.operationSet.put("roll", new Roll());
        }
    }
    
}
