package com.thanhnd.clinic_application.modules.rooms.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.ValidationMessage;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomDto extends BaseDto {
	private String id;

	@NotBlank(message = ValidationMessage.ROOM_NAME_REQUIRED)
	@Size(min = 3, max = 50, message = ValidationMessage.ROOM_NAME_LENGTH)
	private String name;

	@NotNull(message = ValidationMessage.ROOM_DEPARTMENT_REQUIRED)
	private DepartmentDto department;
}
