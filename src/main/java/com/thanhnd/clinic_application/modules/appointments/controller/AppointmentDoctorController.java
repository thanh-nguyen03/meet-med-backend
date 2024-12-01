package com.thanhnd.clinic_application.modules.appointments.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.APPOINTMENT_DOCTOR_CONTROLLER)
@RequiredArgsConstructor
public class AppointmentDoctorController extends BaseController {
	private final AppointmentService appointmentService;

	@GetMapping
	public ResponseEntity<ResponseDto> getDoctorAppointments(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "registeredShiftTimeSlot.startTime") String orderBy,
		@RequestParam(value = PaginationConstants.ORDER) String order,
		@RequestParam(value = "registeredShiftId", required = false) String registeredShiftId,
		@RequestParam(value = "status", required = false) AppointmentStatus status
	) {
		PageRequest pageRequest = parsePageRequest(page, size, orderBy, order);
		return createSuccessResponse(ResponseDto.success(
			appointmentService.findAllForDoctor(pageRequest, registeredShiftId, status)
		));
	}

	@GetMapping("/{appointmentId}")
	public ResponseEntity<ResponseDto> getAppointmentById(@PathVariable String appointmentId) {
		return createSuccessResponse(ResponseDto.success(appointmentService.findByIdForDoctor(appointmentId)));
	}

	@PutMapping("/{appointmentId}/complete")
	public ResponseEntity<ResponseDto> completeAppointment(@PathVariable String appointmentId) {
		return createSuccessResponse(ResponseDto.success(
			appointmentService.updateStatus(appointmentId, AppointmentStatus.COMPLETED))
		);
	}
}
