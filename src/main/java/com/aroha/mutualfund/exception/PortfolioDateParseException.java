package com.aroha.mutualfund.exception;

public class PortfolioDateParseException extends RuntimeException {

	private final String fileName;

	public PortfolioDateParseException(String message, String fileName) {
		super(message);
		this.fileName = fileName;

	}
	public String getFileName() {
        return fileName;
    }
}
