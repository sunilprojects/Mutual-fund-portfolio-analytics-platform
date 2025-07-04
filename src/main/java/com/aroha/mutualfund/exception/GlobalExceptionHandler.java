package com.aroha.mutualfund.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(FileFormatException.class)
	public ResponseEntity<ErrorDetails> handleFileFormatException(FileFormatException ex) {

		log.error("Error:{}, File Name:{}",ex.getMessage(),ex.getFileName());
		ErrorDetails errorDetails = ErrorDetails.builder()
				.timestamp(LocalDateTime.now())
				.fileName(ex.getFileName())
				.message(ex.getMessage())
				.status(HttpStatus.BAD_REQUEST.name())
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
		
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
	

	@ExceptionHandler( ExcelProcessingException.class)
	public ResponseEntity<ErrorDetails> handleExcelProcessingException( ExcelProcessingException ex) {
		ErrorDetails errorDetails = ErrorDetails.builder()
				.timestamp(LocalDateTime.now())
				.fileName(ex.getFileName())
				.message(ex.getMessage())
				.status(HttpStatus.BAD_REQUEST.name())
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.build();
		return new ResponseEntity<>(errorDetails,HttpStatus.BAD_REQUEST);
	}
	
	
	@ExceptionHandler(DBOperationFailureException.class)
	public ResponseEntity<ErrorDetails> handleDBOperationFailureException(DBOperationFailureException ex) {
		ErrorDetails errorDetails = ErrorDetails.builder()
				.timestamp(LocalDateTime.now())
				.fileName("NA")
				.message(ex.getMessage())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.build();

		return new ResponseEntity<>(errorDetails,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
		ErrorDetails errorDetails = ErrorDetails.builder()
				.timestamp(LocalDateTime.now())
				.fileName("NA")
				.message(ex.getMessage())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.build();

		return new ResponseEntity<>(errorDetails,HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
	
