package com.thanhnd.clinic_application.modules.doctors.dto;

import com.thanhnd.clinic_application.constants.ValidationMessage;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDoctorDto {
	@NotNull(message = ValidationMessage.DOCTOR_YEARS_OF_EXPERIENCE_REQUIRED)
	@Min(value = 1, message = ValidationMessage.DOCTOR_YEARS_OF_EXPERIENCE_INVALID)
	private Integer yearsOfExperience;

	@NotBlank(message = ValidationMessage.DOCTOR_DEGREE_REQUIRED)
	private String degree;

	@NotBlank(message = ValidationMessage.DOCTOR_NUMBER_OF_PATIENTS_REQUIRED)
	private Integer numberOfPatients;

	@NotBlank(message = ValidationMessage.DOCTOR_NUMBER_OF_CERTIFICATES_REQUIRED)
	private Integer numberOfCertificates;

	@NotBlank(message = ValidationMessage.DOCTOR_DESCRIPTION_REQUIRED)
	@Size(max = 2000, message = ValidationMessage.DOCTOR_DESCRIPTION_LENGTH)
	private String description;

	@NotNull(message = ValidationMessage.USER_REQUIRED)
	private UserDto user;
}
