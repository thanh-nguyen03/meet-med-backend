package com.thanhnd.clinic_application.modules.doctors.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ControllerPath.DOCTOR_CONTROLLER)
@RequiredArgsConstructor
public class DoctorController extends BaseController {
	private final DoctorService doctorService;

	@GetMapping
	@PermissionsAllowed(permissions = {Permissions.Doctors.READ})
	public ResponseEntity<ResponseDto> findAll(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "createdAt") String orderBy,
		@RequestParam(value = PaginationConstants.ORDER, defaultValue = "asc") String order,
		@RequestParam(value = "name", defaultValue = "") String searchName,
		@RequestParam(value = "department", required = false) String searchDepartment
	) {
		PageRequest pageRequest = parsePageRequest(page, size, orderBy, order);
		return createSuccessResponse(ResponseDto.success(doctorService.findAll(pageRequest, searchName, searchDepartment)));
	}
}
