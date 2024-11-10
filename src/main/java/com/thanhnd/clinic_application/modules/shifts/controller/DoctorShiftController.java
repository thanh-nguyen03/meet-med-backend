package com.thanhnd.clinic_application.modules.shifts.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.shifts.dto.request.RegisterShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftService;
import com.thanhnd.clinic_application.modules.shifts.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ControllerPath.DOCTOR_SHIFT_CONTROLLER)
@RequiredArgsConstructor
public class DoctorShiftController extends BaseController {
	private final ShiftService shiftService;
	private final RegisteredShiftService registeredShiftService;

	@GetMapping("/can-register")
	@PermissionsAllowed(permissions = {Permissions.RegisteredShift.READ})
	public ResponseEntity<ResponseDto> getCanRegisterShifts() {
		return createSuccessResponse(ResponseDto.success(shiftService.getListShiftCanRegister()));
	}

	@GetMapping("/get-week")
	@PermissionsAllowed(permissions = {Permissions.RegisteredShift.READ})
	public ResponseEntity<ResponseDto> getCurrentWeekShifts(@RequestParam(required = false, defaultValue = "false") Boolean isNextWeek) {
		return createSuccessResponse(ResponseDto.success(shiftService.getWeekShiftCanRegister(isNextWeek)));
	}

	@PostMapping("/register")
	@PermissionsAllowed(permissions = {Permissions.RegisteredShift.WRITE})
	public ResponseEntity<ResponseDto> registerShifts(@RequestBody List<@Valid RegisterShiftRequestDto> registerShiftsRequestDto) {
		return createSuccessResponse(ResponseDto.success(registeredShiftService.create(registerShiftsRequestDto)));
	}
}
