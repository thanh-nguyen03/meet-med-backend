package com.thanhnd.clinic_application.modules.departments.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.departments.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ControllerPath.DEPARTMENT_CONTROLLER)
@RequiredArgsConstructor
public class DepartmentController extends BaseController {
	private final DepartmentService departmentService;

	@GetMapping
	public ResponseEntity<ResponseDto> findAll() {
		return createSuccessResponse(ResponseDto.success(departmentService.findAll()));
	}
}
