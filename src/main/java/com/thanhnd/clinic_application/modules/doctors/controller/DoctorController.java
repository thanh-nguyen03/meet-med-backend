package com.thanhnd.clinic_application.modules.doctors.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.doctors.dto.UpdateDoctorDto;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.DOCTOR_CONTROLLER)
@RequiredArgsConstructor
public class DoctorController extends BaseController {
	private final DoctorService doctorService;
	private final RegisteredShiftService registeredShiftService;

	@GetMapping
	@PermissionsAllowed(permissions = {Permissions.Doctors.READ})
	public ResponseEntity<ResponseDto> findAll(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "createdAt") String orderBy,
		@RequestParam(value = PaginationConstants.ORDER, defaultValue = "asc") String order,
		@RequestParam(value = "search", defaultValue = "") String search,
		@RequestParam(value = "department", required = false) String searchDepartment
	) {
		PageRequest pageRequest = parsePageRequest(page, size, orderBy, order);
		return createSuccessResponse(ResponseDto.success(doctorService.findAll(pageRequest, search, searchDepartment)));
	}

	@GetMapping("/my-profile")
	public ResponseEntity<ResponseDto> getMyProfile() {
		String userId = jwtAuthenticationManager.getUserId();
		return createSuccessResponse(ResponseDto.success(doctorService.findByUserId(userId)));
	}

	@PutMapping("/my-profile")
	public ResponseEntity<ResponseDto> updateMyProfile(
		@RequestBody UpdateDoctorDto updateDoctorDto
	) {
		String userId = jwtAuthenticationManager.getUserId();
		return createSuccessResponse(ResponseDto.success(doctorService.update(userId, updateDoctorDto)));
	}

	@GetMapping("/{doctorId}/booking-shifts")
	@PermissionsAllowed(permissions = {Permissions.Doctors.READ})
	public ResponseEntity<ResponseDto> getDoctorShifts(
		@PathVariable String doctorId
	) {
		return createSuccessResponse(ResponseDto.success(registeredShiftService.findAllByDoctorForBooking(doctorId)));
	}
}
