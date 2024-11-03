package com.thanhnd.clinic_application.modules.shifts.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftDto extends BaseDto {
	private String id;
	private Instant startTime;
	private Instant endTime;
}
