package com.thanhnd.clinic_application.common.exception;

import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.Message;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@Slf4j
public class DefaultExceptionHandler extends AbstractErrorController {
	public DefaultExceptionHandler(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@RequestMapping(value = "${error.path:/error}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ResponseDto> error(HttpServletRequest request) {
		HttpStatus status = getStatus(request);
		String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

		if (exception != null && exception.getCause() != null) {
			exception = exception.getCause();
		}

		if (exception instanceof HttpException) {
			return createErrorResponse(ResponseDto.of((HttpException) exception));
		}

		return createErrorResponse(ResponseDto.of(
			Objects.requireNonNullElse(status, HttpStatus.INTERNAL_SERVER_ERROR).value(),
			false,
			Objects.requireNonNullElse(errorMessage, Message.INTERNAL_SERVER_ERROR.getMessage()),
			null
		));
	}

	public ResponseEntity<ResponseDto> createErrorResponse(ResponseDto message) {
		return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatusCode()));
	}
}
