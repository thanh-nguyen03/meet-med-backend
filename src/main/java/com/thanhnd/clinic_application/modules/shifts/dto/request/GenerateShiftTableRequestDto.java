package com.thanhnd.clinic_application.modules.shifts.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateShiftTableRequestDto {
	@NotNull
	@Min(1)
	@Max(12)
	private Integer month;

	@NotNull
	private int year;
}
