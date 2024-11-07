package com.thanhnd.clinic_application.modules.shifts.dto.request;

import com.thanhnd.clinic_application.constants.ValidationMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterShiftRequestDto {
	@NotBlank(message = ValidationMessage.SHIFT_REQUIRED)
	private String id;
}
