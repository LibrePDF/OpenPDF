package com.sun.pdfview.action;

import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/*****************************************************************************
 * Action for launching an application, mostly used to open a file.
 *
 * @author  Katja Sondermann
 * @since 08.07.2009
 ****************************************************************************/
public class LaunchAction extends PDFAction {
	// file separator according to PDF spec
	public final static String SOLIDUS = "/";

	/** the file/application to be opened (optional)*/
	private FileSpec file;
	/** should a new window be opened (optional)*/
	private boolean newWindow = false;
	private PDFObject unixParam;
	private PDFObject macParam;
	private WinLaunchParam winParam;

	/** 
	 * Creates a new instance of LaunchAction from an object
	 *
	 * @param obj - the PDFObject with the action information
	 * @param root - the root object
	 */
	public LaunchAction(PDFObject obj, PDFObject root) throws IOException {
		super("Launch");
		// find the file/application and parse it
		PDFObject fileObj = obj.getDictRef("F");
		this.file = parseFileSpecification(fileObj);

		// find the new window flag and parse it
		PDFObject newWinObj = obj.getDictRef("NewWindow");
		if (newWinObj != null) {
			this.newWindow = newWinObj.getBooleanValue();
		}
		// parse the OS specific launch parameters:
		this.winParam = parseWinDict(obj.getDictRef("Win"));
		// unix and mac dictionaries are not further specified, so can not be parsed yet.
		this.unixParam = obj.getDictRef("Unix");
		this.macParam = obj.getDictRef("Mac");

		// check if at least the file or one of the OS specific launch parameters is set:
		if ((this.file == null) 
			&& (this.winParam == null) 
			&& (this.unixParam == null)
			&& (this.macParam == null)) {
			throw new PDFParseException("Could not parse launch action (file or OS " +
					"specific launch parameters are missing): " + obj.toString());
		}
	}

	/*************************************************************************
	 * Is the file name absolute (if not, it is relative to the path of the 
	 * currently opened PDF file).
	 * If the file name starts with a "/", it is considered to be absolute.
	 * 
	 * @return boolean
	 ************************************************************************/
	public static boolean isAbsolute(String fileName) {			
		return fileName.startsWith(SOLIDUS);
	}

	/*************************************************************************
	 * Parse the file specification object
	 * @param fileObj
	 * @return FileSpec - might be <code>null</code> in case the passed object is null 
	 * @throws IOException 
	 * @throws PDFParseException 
	 ************************************************************************/
	private FileSpec parseFileSpecification(PDFObject fileObj) throws PDFParseException, IOException {
		FileSpec file = null;
		if (fileObj != null) {
			file = new FileSpec(); 
			if(fileObj.getType() == PDFObject.DICTIONARY){
				file.setFileSystem(PdfObjectParseUtil.parseStringFromDict("FS", fileObj, false));
				file.setFileName(PdfObjectParseUtil.parseStringFromDict("F", fileObj, false));
				file.setUnicode(PdfObjectParseUtil.parseStringFromDict("UF", fileObj, false));
				file.setDosFileName(PdfObjectParseUtil.parseStringFromDict("DOS", fileObj, false));
				file.setMacFileName(PdfObjectParseUtil.parseStringFromDict("Mac", fileObj, false));
				file.setUnixFileName(PdfObjectParseUtil.parseStringFromDict("Unix", fileObj, false));
				file.setVolatileFile(PdfObjectParseUtil.parseBooleanFromDict("V", fileObj, false));
				file.setDescription(PdfObjectParseUtil.parseStringFromDict("Desc", fileObj, false));
				file.setId(fileObj.getDictRef("ID"));
				file.setEmbeddedFile(fileObj.getDictRef("EF"));
				file.setRelatedFile(fileObj.getDictRef("RF"));
				file.setCollectionItem(fileObj.getDictRef("CI"));
			}else if(fileObj.getType() == PDFObject.STRING){
				file.setFileName(fileObj.getStringValue());
			}else{
				throw new PDFParseException("File specification could not be parsed " +
					"(should be of type 'Dictionary' or 'String'): " + fileObj.toString());
			}
		}
		return file;
	}
	

	/*************************************************************************
	 * Parse the windows specific launch parameters 
	 * @param winDict
	 * @throws IOException - in case of a problem during parsing content 
	 ************************************************************************/
	private WinLaunchParam parseWinDict(PDFObject winDict) throws IOException {
		if (winDict == null) {
			return null;
		}
		WinLaunchParam param = new WinLaunchParam();

		// find and parse the file/application name
		param.setFileName(PdfObjectParseUtil.parseStringFromDict("F", winDict, true));

		// find and parse the directory
		param.setDirectory(PdfObjectParseUtil.parseStringFromDict("D", winDict, false));

		// find and parse the operation to be performed
		param.setOperation(PdfObjectParseUtil.parseStringFromDict("O", winDict, false));

		// find and parse the parameter to be passed to the application
		param.setParameter(PdfObjectParseUtil.parseStringFromDict("P", winDict, false));

		return param;
	}

	/*************************************************************************
	 * The file / application to be opened
	 * @return FileSpec
	 ************************************************************************/
	public FileSpec getFileSpecification() {
		return this.file;
	}

	/*************************************************************************
	 * Should a new window be opened for the file/application?
	 * @return boolean
	 ************************************************************************/
	public boolean isNewWindow() {
		return this.newWindow;
	}

	/*************************************************************************
	 * Get the unix specific launch parameters.
	 * Note: The dictionary is not specified yet in the PDF spec., so the PdfObject 
	 * 		which is returned here is not parsed.
	 * @return PDFObject
	 ************************************************************************/
	public PDFObject getUnixParam() {
		return this.unixParam;
	}

	/*************************************************************************
	 * Get the mac specific launch parameters.
	 * Note: The dictionary is not specified yet in the PDF spec., so the PdfObject 
	 * 		which is returned here is not parsed.
	 * @return PDFObject
	 ************************************************************************/
	public PDFObject getMacParam() {
		return this.macParam;
	}

	/*************************************************************************
	 * Get the windows specific launch parameters.
	 * @return WinLaunchParam
	 ************************************************************************/
	public WinLaunchParam getWinParam() {
		return this.winParam;
	}

	/*****************************************************************************
	 * Internal class for the windows specific launch parameters
	 *
	 * @version $Id: LaunchAction.java,v 1.1 2009-07-10 12:47:31 xond Exp $ 
	 * @author  xond
	 * @since 08.07.2009
	 ****************************************************************************/
	public class WinLaunchParam {
		private String fileName;
		private String directory;
		private String operation = "open";
		private String parameter;

		/*************************************************************************
		 * The file/application name to be opened
		 * @return String
		 ************************************************************************/
		public String getFileName() {
			return this.fileName;
		}

		/*************************************************************************
		 * The file/application name to be opened
		 * @param fileName
		 ************************************************************************/
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		/*************************************************************************
		 * The directory in standard DOS syntax
		 * @return String
		 ************************************************************************/
		public String getDirectory() {
			return this.directory;
		}

		/*************************************************************************
		 * The directory in standard DOS syntax
		 * @param directory
		 ************************************************************************/
		public void setDirectory(String directory) {
			this.directory = directory;
		}

		/*************************************************************************
		 * The operation to be performed (open or print). Ignored
		 * in case the "F" parameter describes a file to be opened.
		 * Default is "open".
		 * @return String
		 ************************************************************************/
		public String getOperation() {
			return this.operation;
		}

		/*************************************************************************
		 * The operation to be performed ("open" or "print").Ignored
		 * in case the "F" parameter describes a file to be opened.
		 * Default is "open".
		 * @param operation
		 ************************************************************************/
		public void setOperation(String operation) {
			this.operation = operation;
		}

		/*************************************************************************
		 * A parameter which shall be passed to the application. Ignored
		 * in case the "F" parameter describes a file to be opened.
		 * @return String
		 ************************************************************************/
		public String getParameter() {
			return this.parameter;
		}

		/*************************************************************************
		 * A parameter which shall be passed to the application. Ignored
		 * in case the "F" parameter describes a file to be opened.
		 * @param parameter
		 ************************************************************************/
		public void setParameter(String parameter) {
			this.parameter = parameter;
		}
	}
	
	/*****************************************************************************
	 * Inner class for storing a file specification
	 *
	 * @version $Id: LaunchAction.java,v 1.1 2009-07-10 12:47:31 xond Exp $ 
	 * @author  xond
	 * @since 08.07.2009
	 ****************************************************************************/
	public static class FileSpec{
		private String fileSystem;
		private String fileName;
		private String dosFileName;
		private String unixFileName;
		private String macFileName;
		private String unicode;
		private PDFObject id;
		private boolean volatileFile;
		private PDFObject embeddedFile;
		private PDFObject relatedFile;
		private String description;
		private PDFObject collectionItem;
		
		/*************************************************************************
		 * The name of the file system that should be used to interpret this entry.
		 * @return String 
		 ************************************************************************/
		public String getFileSystem() {
			return this.fileSystem;
		}

		/*************************************************************************
		 * The name of the file system that should be used to interpret this entry.
		 * @param fileSystem 
		 ************************************************************************/
		public void setFileSystem(String fileSystem) {
			this.fileSystem = fileSystem;
		}
		
		/*************************************************************************
		 * Get the filename:
		 * first try to get the file name for the used OS, if it's not available
		 * return the common file name.
		 * @return String 
		 ************************************************************************/
		public String getFileName() {
			String system = System.getProperty("os.name");
			if(system.startsWith("Windows")){
				if(this.dosFileName != null){
					return this.dosFileName;
				}
			}else if(system.startsWith("mac os x")){
				if(this.macFileName != null){
					return this.macFileName;
				}
			}else {
				if(this.unixFileName != null){
					return this.unixFileName;
				}
			}
			return this.fileName;
		}

		/*************************************************************************
		 * The file name.
		 * @param fileName 
		 ************************************************************************/
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		
		/*************************************************************************
		 * A file specification string representing a DOS file name.
		 * @return String
		 ************************************************************************/
		public String getDosFileName() {
			return this.dosFileName;
		}

		/*************************************************************************
		 * A file specification string representing a DOS file name.
		 * @param dosFileName
		 ************************************************************************/
		public void setDosFileName(String dosFileName) {
			this.dosFileName = dosFileName;
		}
		
		/*************************************************************************
		 * A file specification string representing a unix file name.
		 * @return String
		 ************************************************************************/
		public String getUnixFileName() {
			return this.unixFileName;
		}

		/*************************************************************************
		 * A file specification string representing a unix file name.
		 * @param unixFileName
		 ************************************************************************/
		public void setUnixFileName(String unixFileName) {
			this.unixFileName = unixFileName;
		}

		/*************************************************************************
		 * A file specification string representing a mac file name.
		 * @return String
		 ************************************************************************/
		public String getMacFileName() {
			return this.macFileName;
		}
		
		/*************************************************************************
		 * A file specification string representing a mac file name.
		 * @param macFileName
		 ************************************************************************/
		public void setMacFileName(String macFileName) {
			this.macFileName = macFileName;
		}
		
		/*************************************************************************
		 * Unicode file name
		 * @return String
		 ************************************************************************/
		public String getUnicode() {
			return this.unicode;
		}

		/*************************************************************************
		 * Unicode file name
		 * @param unicode
		 ************************************************************************/
		public void setUnicode(String unicode) {
			this.unicode = unicode;
		}
		
		/*************************************************************************
		 * ID - array of two byte strings constituting a file identifier, which 
		 * should be included in the referenced file.
		 * 
		 * @return PDFObject
		 ************************************************************************/
		public PDFObject getId() {
			return this.id;
		}
		
		/*************************************************************************
		 * ID - array of two byte strings constituting a file identifier, which 
		 * should be included in the referenced file.
		 * 
		 * @param id
		 ************************************************************************/
		public void setId(PDFObject id) {
			this.id = id;
		}
		
		/*************************************************************************
		 * Is the file volatile?
		 * @return boolean
		 ************************************************************************/
		public boolean isVolatileFile() {
			return this.volatileFile;
		}
		
		/*************************************************************************
		 * Is the file volatile?
		 * @param volatileFile
		 ************************************************************************/
		public void setVolatileFile(boolean volatileFile) {
			this.volatileFile = volatileFile;
		}
		
		/*************************************************************************
		 * Dictionary of embedded file streams
		 * @return PDFObject
		 ************************************************************************/
		public PDFObject getEmbeddedFile() {
			return this.embeddedFile;
		}

		/*************************************************************************
		 * Dictionary of embedded file streams
		 * @param embeddedFile
		 ************************************************************************/
		public void setEmbeddedFile(PDFObject embeddedFile) {
			this.embeddedFile = embeddedFile;
		}
		
		/*************************************************************************
		 * Dictionary of related files. 
		 * @return PDFObject
		 ************************************************************************/
		public PDFObject getRelatedFile() {
			return this.relatedFile;
		}

		/*************************************************************************
		 * Dictionary of related files. 
		 * @param relatedFile
		 ************************************************************************/
		public void setRelatedFile(PDFObject relatedFile) {
			this.relatedFile = relatedFile;
		}
		
		/*************************************************************************
		 * File specification description
		 * @return String
		 ************************************************************************/
		public String getDescription() {
			return this.description;
		}

		/*************************************************************************
		 * File specification description
		 * @param description
		 ************************************************************************/
		public void setDescription(String description) {
			this.description = description;
		}
		
		/*************************************************************************
		 * Collection item dictionary
		 * @return PDFObject
		 ************************************************************************/
		public PDFObject getCollectionItem() {
			return this.collectionItem;
		}

		/*************************************************************************
		 * Collection item dictionary
		 * @param collectionItem
		 ************************************************************************/
		public void setCollectionItem(PDFObject collectionItem) {
			this.collectionItem = collectionItem;
		}
	}
}
