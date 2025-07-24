package com.aroha.mutualfund.exception;

public class AWSS3FileUploadException extends RuntimeException {

	private final String fileName;
	private final String message;

	public AWSS3FileUploadException(String message, String fileName) {
		super(message);
		this.fileName = fileName;
		this.message = message;
	}

	public String getFileName() {
		return fileName;
	}

}
