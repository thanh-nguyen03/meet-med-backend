package com.thanhnd.clinic_application.modules.shifts.dto.request;

import com.thanhnd.clinic_application.constants.ValidationMessage;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRegisteredShiftRequestDto {
	@NotNull(message = ValidationMessage.ROOM_IS_REQUIRED)
	private RoomDto room;
}
