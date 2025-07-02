package com.aroha.mutualfund.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDetails {
	private LocalDateTime timestamp;
	private String message;
	private String fileName;
	private String status;
	private int statusCode;
}
