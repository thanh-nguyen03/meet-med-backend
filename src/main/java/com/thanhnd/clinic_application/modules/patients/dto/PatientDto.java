package com.thanhnd.clinic_application.modules.patients.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.ValidationMessage;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude
public class PatientDto extends BaseDto {
	private String id;
	private String addressLine;
	private String district;
	private String city;
	private String insuranceCode;

	@NotNull(message = ValidationMessage.USER_REQUIRED)
	private UserDto user;
}
