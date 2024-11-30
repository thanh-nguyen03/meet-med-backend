package com.thanhnd.clinic_application.modules.appointments.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.APPOINTMENT_CONTROLLER)
@RequiredArgsConstructor
public class AppointmentController extends BaseController {
	private final AppointmentService appointmentService;
	private final JwtAuthenticationManager jwtAuthenticationManager;

	@GetMapping
	public ResponseEntity<ResponseDto> getMyAppointments(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "createdAt") String orderBy,
		@RequestParam(value = PaginationConstants.ORDER) String order,
		@RequestParam(value = "status", required = false) AppointmentStatus status
	) {
		String userId = jwtAuthenticationManager.getUserId();
		PageRequest pageRequest = parsePageRequest(page, size, orderBy, order);
		return createSuccessResponse(ResponseDto.success(appointmentService.findAllByPatientUserId(pageRequest, userId, status)));
	}

	@GetMapping("/{appointmentId}")
	public ResponseEntity<ResponseDto> getAppointmentById(@PathVariable String appointmentId) {
		return createSuccessResponse(ResponseDto.success(appointmentService.findById(appointmentId)));
	}

	@PostMapping
	public ResponseEntity<ResponseDto> createAppointment(@RequestBody @Valid AppointmentDto appointmentDto) {
		return createSuccessResponse(ResponseDto.success(appointmentService.create(appointmentDto)));
	}

	@PutMapping("/{appointmentId}")
	public ResponseEntity<ResponseDto> updateAppointment(
		@PathVariable String appointmentId,
		@RequestBody @Valid AppointmentDto appointmentDto
	) {
		appointmentDto.setId(appointmentId);
		return createSuccessResponse(ResponseDto.success(appointmentService.update(appointmentDto)));
	}

	@DeleteMapping("/{appointmentId}")
	public ResponseEntity<ResponseDto> deleteAppointment(@PathVariable String appointmentId) {
		appointmentService.delete(appointmentId);
		return createSuccessResponse(ResponseDto.success());
	}
}
