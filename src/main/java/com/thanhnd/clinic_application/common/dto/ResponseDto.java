package com.thanhnd.clinic_application.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thanhnd.clinic_application.common.constants.Message;
import com.thanhnd.clinic_application.common.exception.HttpException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseDto {
	private int statusCode;
	private boolean success;
	private String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Object data;

	public ResponseDto(int statusCode, boolean success, String message, Object data) {
		this.statusCode = statusCode;
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public static ResponseDto of(int statusCode, boolean success, String message, Object data) {
		return new ResponseDto(statusCode, success, message, data);
	}

	public static ResponseDto of(boolean success, String message, Object data) {
		return of(HttpStatus.OK.value(), success, message, data);
	}

	public static ResponseDto of(HttpException httpException) {
		return of(httpException.getStatus().value(), false, httpException.getMessage(), null);
	}

	public static ResponseDto success(String message, Object data) {
		return of(HttpStatus.OK.value(), true, message, data);
	}

	public static ResponseDto success(Object data) {
		return success(Message.OK.getMessage(), data);
	}

	public static ResponseDto success() {
		return success(null);
	}

	public static ResponseDto badRequest(String message) {
		return of(HttpStatus.BAD_REQUEST.value(), false, message, null);
	}

	public static ResponseDto forbidden(String message) {
		return of(HttpStatus.FORBIDDEN.value(), false, message, null);
	}

	public static ResponseDto notFound(String message) {
		return of(HttpStatus.NOT_FOUND.value(), false, message, null);
	}
}
