package com.aroha.mutualfund.exception;

public class MutualFundProcessingException extends RuntimeException {
	private final String fileName;

	public MutualFundProcessingException(String message, String fileName) {
		super(message);
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}