package com.thanhnd.clinic_application.modules.shifts.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegisteredShiftTimeSlotDto extends BaseDto {
	private String id;
	private String startTime;
	private String endTime;
}
