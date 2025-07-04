package com.aroha.mutualfund.exception;

public class FundValidationException extends RuntimeException {
	private final String fileName;

	public FundValidationException(String message, String fileName) {
		super(message);
		this.fileName = fileName;

	}
	public String getFileName() {
        return fileName;
    }
	
}