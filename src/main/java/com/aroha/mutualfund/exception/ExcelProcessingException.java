package com.aroha.mutualfund.exception;

public class ExcelProcessingException extends RuntimeException {
	
	  private final String fileName;

	    public ExcelProcessingException(String message, String fileName) {
	        super(message);
	        this.fileName=fileName;
	    }
	    public String getFileName() {
	        return fileName;
	    }

}
