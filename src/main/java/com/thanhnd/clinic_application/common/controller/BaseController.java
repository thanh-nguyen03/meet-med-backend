package com.thanhnd.clinic_application.common.controller;

import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
public abstract class BaseController {
	@Autowired
	protected JwtAuthenticationManager jwtAuthenticationManager;

	public ResponseEntity<ResponseDto> createSuccessResponse(ResponseDto responseDto) {
		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> createResponse(HttpStatus httpStatus, ResponseDto responseDto) {
		return new ResponseEntity<>(responseDto, httpStatus);
	}

	public ResponseEntity<ResponseDto> createErrorResponse(ResponseDto message) {
		return new ResponseEntity<>(message, HttpStatus.valueOf(message.getStatusCode()));
	}

	protected static PageRequest parsePageRequest(int page, int size, String sortBy, String order) {
		Sort.Direction sortDirection = order.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
	}
}
