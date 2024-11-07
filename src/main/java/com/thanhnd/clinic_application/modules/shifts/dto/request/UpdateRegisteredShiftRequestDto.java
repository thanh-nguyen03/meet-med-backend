package com.thanhnd.clinic_application.modules.shifts.dto.request;

import lombok.Getter;

@Getter
public class UpdateRegisteredShiftRequestDto {
	private Double shiftPrice;
	private Integer maxNumberOfPatients;
}
