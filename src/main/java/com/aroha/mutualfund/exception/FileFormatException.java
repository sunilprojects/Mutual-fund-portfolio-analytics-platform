package com.aroha.mutualfund.exception;

public class FileFormatException extends RuntimeException{

	private final String fileName;
	
	public FileFormatException(String message,String fileName) {
        super(message);
        this.fileName=fileName;
    }
	
	public String getFileName() {
        return fileName;
    }
	
}