package com.aroha.mutualfund.exception;

public class FileFormatException extends RuntimeException{

	private final String fileName;
	private final String message;
	
	public FileFormatException(String message,String fileName) {
        super(message);
        this.fileName=fileName;
        this.message=message;
    }
	
	public String getFileName() {
        return fileName;
    }
	
}