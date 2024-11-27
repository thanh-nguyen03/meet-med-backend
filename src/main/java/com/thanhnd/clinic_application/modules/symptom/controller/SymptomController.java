package com.thanhnd.clinic_application.modules.symptom.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.symptom.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ControllerPath.SYMPTOM_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class SymptomController extends BaseController {
	private final SymptomService symptomService;

	@GetMapping
	public ResponseEntity<ResponseDto> findAll() {
		return createSuccessResponse(ResponseDto.success(symptomService.findAll()));
	}
}
