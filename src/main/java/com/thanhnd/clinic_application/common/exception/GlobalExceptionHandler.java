package com.thanhnd.clinic_application.common.exception;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseController {
	@ExceptionHandler(value = {HttpException.class})
	public ResponseEntity<ResponseDto> handleUnknownException(HttpServletRequest request, HttpException e) {
		log.error("HTTP {} Exception @{}: {}", e.getStatus().getReasonPhrase(), request.getRequestURI(), e.getMessage());
		log.trace("Stack trace", e);
		return createErrorResponse(ResponseDto.of(e));
	}
}
