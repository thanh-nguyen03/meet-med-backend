package com.thanhnd.clinic_application.common.exception;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseController {
	@ExceptionHandler(value = {HttpException.class})
	public ResponseEntity<ResponseDto> handleUnknownException(HttpServletRequest request, HttpException e) {
		log.error("HTTP {} Exception @{}: {}", e.getStatus().getReasonPhrase(), request.getRequestURI(), e.getMessage());
		log.trace("Stack trace", e);
		return createErrorResponse(ResponseDto.of(e));
	}

	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	public ResponseEntity<ResponseDto> handleSpringValidationException(HttpServletRequest request, MethodArgumentNotValidException e) {
		log.error("Validation Errror @{}: {}", request.getRequestURI(), e.getMessage());
		return createResponse(
			HttpStatus.BAD_REQUEST,
			ResponseDto.of(
				false,
				"Invalid argument(s)!",
				e.getBindingResult()
					.getAllErrors()
					.stream()
					.map(err -> new ValidationMessage(err.getDefaultMessage())).collect(Collectors.toList())
			)
		);
	}

	@Getter
	private static class ValidationMessage {
		private final String errorMessage;

		ValidationMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}
}
