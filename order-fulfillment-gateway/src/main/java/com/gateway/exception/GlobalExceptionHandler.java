package com.gateway.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(PipelineValidationException.class)
	public ResponseEntity<Map<String, String>> handleValidation(PipelineValidationException exception) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(Map.of("error", exception.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
	}
}
