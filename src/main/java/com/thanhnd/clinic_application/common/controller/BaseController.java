package com.thanhnd.clinic_application.common.controller;

import com.thanhnd.clinic_application.common.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
	public ResponseEntity<ResponseDto> createSuccessResponse(ResponseDto responseDto) {
		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> createResponse(HttpStatus httpStatus, ResponseDto responseDto) {
		return new ResponseEntity<>(responseDto, httpStatus);
	}

	public ResponseEntity<ResponseDto> createErrorResponse(ResponseDto message) {
		return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatusCode()));
	}
}
