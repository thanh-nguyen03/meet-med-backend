package com.thanhnd.clinic_application.modules.departments.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.departments.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.DEPARTMENT_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class DepartmentAdminController extends BaseController {
	private final DepartmentService departmentService;

	@GetMapping
	@PermissionsAllowed(Permissions.Categories.READ)
	public ResponseEntity<ResponseDto> findAll() {
		return createSuccessResponse(ResponseDto.success(departmentService.findAll()));
	}

	@GetMapping("/{id}")
	@PermissionsAllowed(Permissions.Categories.READ)
	public ResponseEntity<ResponseDto> findById(@PathVariable String id) {
		return createSuccessResponse(ResponseDto.success(departmentService.findById(id)));
	}

	@PostMapping
	@PermissionsAllowed(Permissions.Categories.WRITE)
	public ResponseEntity<ResponseDto> create(@RequestBody @Valid DepartmentDto departmentDto) {
		return createSuccessResponse(ResponseDto.success(departmentService.create(departmentDto)));
	}

	@PutMapping("/{id}")
	@PermissionsAllowed(Permissions.Categories.WRITE)
	public ResponseEntity<ResponseDto> update(@PathVariable String id, @RequestBody @Valid DepartmentDto departmentDto) {
		return createSuccessResponse(ResponseDto.success(departmentService.update(id, departmentDto)));
	}

	@DeleteMapping("/{id}")
	@PermissionsAllowed(Permissions.Categories.WRITE)
	public ResponseEntity<ResponseDto> delete(@PathVariable String id) {
		departmentService.delete(id);
		return createSuccessResponse(ResponseDto.success());
	}
}
