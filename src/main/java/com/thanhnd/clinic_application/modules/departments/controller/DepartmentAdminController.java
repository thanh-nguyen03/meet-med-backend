package com.thanhnd.clinic_application.modules.departments.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.departments.dto.AddDoctorsDto;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.departments.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.DEPARTMENT_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class DepartmentAdminController extends BaseController {
	private final DepartmentService departmentService;

	@GetMapping
	@PermissionsAllowed(Permissions.Departments.READ)
	public ResponseEntity<ResponseDto> findAll(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "createdAt") String orderBy,
		@RequestParam(value = PaginationConstants.ORDER, defaultValue = "asc") String order,
		@RequestParam(value = PaginationConstants.SEARCH, defaultValue = "") String search
	) {
		PageRequest pageRequest = parsePageRequest(page, size, orderBy, order);
		return createSuccessResponse(ResponseDto.success(departmentService.findAll(pageRequest)));
	}

	@GetMapping("/{id}")
	@PermissionsAllowed(Permissions.Departments.READ)
	public ResponseEntity<ResponseDto> findById(@PathVariable String id) {
		return createSuccessResponse(ResponseDto.success(departmentService.findById(id)));
	}

	@PostMapping
	@PermissionsAllowed(Permissions.Departments.WRITE)
	public ResponseEntity<ResponseDto> create(@RequestBody @Valid DepartmentDto departmentDto) {
		return createSuccessResponse(ResponseDto.success(departmentService.create(departmentDto)));
	}

	@PutMapping("/{id}")
	@PermissionsAllowed(Permissions.Departments.WRITE)
	public ResponseEntity<ResponseDto> update(@PathVariable String id, @RequestBody @Valid DepartmentDto departmentDto) {
		return createSuccessResponse(ResponseDto.success(departmentService.update(id, departmentDto)));
	}

	@DeleteMapping("/{id}")
	@PermissionsAllowed(Permissions.Departments.WRITE)
	public ResponseEntity<ResponseDto> delete(@PathVariable String id) {
		departmentService.delete(id);
		return createSuccessResponse(ResponseDto.success());
	}

	@PatchMapping("/{departmentId}/head-doctor/{doctorId}")
	public ResponseEntity<ResponseDto> addHeadDoctor(
		@PathVariable String departmentId,
		@PathVariable String doctorId
	) {
		departmentService.addHeadDoctor(departmentId, doctorId);
		return createSuccessResponse(ResponseDto.success());
	}

	@PatchMapping("/{departmentId}/doctors")
	public ResponseEntity<ResponseDto> addDoctors(
		@PathVariable String departmentId,
		@RequestBody @Valid AddDoctorsDto addDoctorsDto
	) {
		departmentService.addDoctors(departmentId, addDoctorsDto.getDoctorIds());
		return createSuccessResponse(ResponseDto.success());
	}

	@PatchMapping("/{departmentId}/doctors/{doctorId}")
	public ResponseEntity<ResponseDto> removeDoctor(
		@PathVariable String departmentId,
		@PathVariable String doctorId
	) {
		departmentService.removeDoctor(departmentId, doctorId);
		return createSuccessResponse(ResponseDto.success());
	}
}
