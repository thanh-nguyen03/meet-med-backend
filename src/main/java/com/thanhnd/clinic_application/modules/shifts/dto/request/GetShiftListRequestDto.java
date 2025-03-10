package com.thanhnd.clinic_application.modules.shifts.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GetShiftListRequestDto {
	@NotNull
	private LocalDate startDate;

	@NotNull
	private LocalDate endDate;
}
