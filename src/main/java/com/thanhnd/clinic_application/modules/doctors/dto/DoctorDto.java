package com.thanhnd.clinic_application.modules.doctors.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.ValidationMessage;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorDto extends BaseDto {
	private String id;
	@NotNull(message = ValidationMessage.DOCTOR_YEARS_OF_EXPERIENCE_REQUIRED)
	@Min(value = 1, message = ValidationMessage.DOCTOR_YEARS_OF_EXPERIENCE_INVALID)
	private Integer yearsOfExperience;

	@NotBlank(message = ValidationMessage.DOCTOR_DEGREE_REQUIRED)
	private String degree;

	@NotBlank(message = ValidationMessage.DOCTOR_DESCRIPTION_REQUIRED)
	@Size(max = 2000, message = ValidationMessage.DOCTOR_DESCRIPTION_LENGTH)
	private String description;

	@Valid
	private UserDto user;

	@Valid
	private DepartmentDto department;
}
