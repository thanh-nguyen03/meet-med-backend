package com.thanhnd.clinic_application.modules.users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.Role;
import com.thanhnd.clinic_application.constants.UserGender;
import com.thanhnd.clinic_application.constants.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude
public class UserDto extends BaseDto {
	private String id;

	@NotBlank(message = ValidationMessage.EMAIL_REQUIRED)
	@Email(message = ValidationMessage.EMAIL_INVALID)
	private String email;

	@NotBlank(message = ValidationMessage.FULL_NAME_REQUIRED)
	@Length(min = 3, max = 50, message = ValidationMessage.FULL_NAME_LENGTH)
	private String fullName;

	private Integer age;
	private String phone;
	private String imageUrl;
	private UserGender gender = UserGender.Male;
	private Role role;
}
