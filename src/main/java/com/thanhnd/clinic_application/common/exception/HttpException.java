package com.thanhnd.clinic_application.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpException extends RuntimeException {
	private final HttpStatus status;
	private final String message;

	private HttpException(String message, HttpStatus status) {
		super(message);
		this.message = message;
		this.status = status;
	}

	public static HttpException notFound(String message) {
		return new HttpException(message, HttpStatus.NOT_FOUND);
	}

	public static HttpException badRequest(String message) {
		return new HttpException(message, HttpStatus.BAD_REQUEST);
	}

	public static HttpException forbidden(String message) {
		return new HttpException(message, HttpStatus.FORBIDDEN);
	}

	public static HttpException internalServerError(String message) {
		return new HttpException(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
