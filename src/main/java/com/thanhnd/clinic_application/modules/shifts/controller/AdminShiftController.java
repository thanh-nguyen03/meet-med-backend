package com.thanhnd.clinic_application.modules.shifts.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.shifts.dto.GenerateShiftTableDto;
import com.thanhnd.clinic_application.modules.shifts.dto.GetShiftListDto;
import com.thanhnd.clinic_application.modules.shifts.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.SHIFT_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class AdminShiftController extends BaseController {
	private final ShiftService shiftService;

	@GetMapping
	public ResponseEntity<ResponseDto> getShiftList(@RequestBody @Valid GetShiftListDto getShiftListDto) {
		return createSuccessResponse(ResponseDto.success(shiftService.getShiftList(getShiftListDto.getStartDate(), getShiftListDto.getEndDate())));
	}

	@PostMapping("/generate")
	public ResponseEntity<ResponseDto> generateShiftTable(@RequestBody @Valid GenerateShiftTableDto dto) {
		shiftService.createShiftTable(dto.getMonth(), dto.getYear());
		return createSuccessResponse(ResponseDto.success());
	}
}
