package com.thanhnd.clinic_application.modules.doctors.dto;

import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.constants.ValidationMessage;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateDoctorDto {
	@NotBlank(message = ValidationMessage.EMAIL_REQUIRED)
	@Email(message = ValidationMessage.EMAIL_INVALID)
	private String email;

	@NotBlank(message = ValidationMessage.PASSWORD_REQUIRED)
	@Size(min = 6, message = ValidationMessage.PASSWORD_LENGTH)
	private String password;

	@NotBlank(message = ValidationMessage.FULL_NAME_REQUIRED)
	@Length(min = 3, max = 50, message = ValidationMessage.FULL_NAME_LENGTH)
	private String fullName;

	@Enumerated(EnumType.STRING)
	private UserGender gender = UserGender.Male;

	@NotNull(message = ValidationMessage.DOCTOR_YEARS_OF_EXPERIENCE_REQUIRED)
	@Min(value = 1, message = ValidationMessage.DOCTOR_YEARS_OF_EXPERIENCE_INVALID)
	private Integer yearsOfExperience;

	@NotBlank(message = ValidationMessage.DOCTOR_DEGREE_REQUIRED)
	private String degree;

	@NotBlank(message = ValidationMessage.DOCTOR_DESCRIPTION_REQUIRED)
	@Size(max = 2000, message = ValidationMessage.DOCTOR_DESCRIPTION_LENGTH)
	private String description;

	@NotBlank(message = ValidationMessage.DEPARTMENT_REQUIRED)
	private String departmentId;

	private String identityProvider;
}
