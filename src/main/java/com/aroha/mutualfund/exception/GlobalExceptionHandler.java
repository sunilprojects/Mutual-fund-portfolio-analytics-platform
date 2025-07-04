package com.aroha.mutualfund.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(FileFormatException.class)
	public ResponseEntity<ErrorDetails> handleFileFormatException(FileFormatException ex) {
		ErrorDetails errorDetails = ErrorDetails.builder().timestamp(LocalDateTime.now()).fileName(ex.getFileName())
				.message(ex.getMessage()).status(HttpStatus.BAD_REQUEST.name())
				.statusCode(HttpStatus.BAD_REQUEST.value()).build();

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MutualFundProcessingException.class)
	public ResponseEntity<ErrorDetails> handleMutualFundProcessingException(MutualFundProcessingException ex) {
		ErrorDetails errorDetails = ErrorDetails.builder().timestamp(LocalDateTime.now()).fileName(ex.getFileName())
				.message(ex.getMessage()).status(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();

		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(FundValidationException.class)
	public ResponseEntity<ErrorDetails> handleFundValidationException(FundValidationException ex) {
		ErrorDetails errorDetails = ErrorDetails.builder().timestamp(LocalDateTime.now()).fileName(ex.getFileName())
				.message(ex.getMessage()).status(HttpStatus.BAD_REQUEST.name())
				.statusCode(HttpStatus.BAD_REQUEST.value()).build();

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}
	@ExceptionHandler(PortfolioDateParseException.class)
	public ResponseEntity<ErrorDetails> handlePortfolioDateParseException(PortfolioDateParseException ex) {
		ErrorDetails errorDetails = ErrorDetails.builder().timestamp(LocalDateTime.now()).fileName(ex.getFileName())
				.message(ex.getMessage()).status(HttpStatus.BAD_REQUEST.name())
				.statusCode(HttpStatus.BAD_REQUEST.value()).build();

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}
}
	
