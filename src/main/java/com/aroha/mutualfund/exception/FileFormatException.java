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
	
<<<<<<< HEAD
}
=======
}
>>>>>>> 53e4651aa86b6cd1566189e881bdb78d875e029b
