package com.thanhnd.clinic_application.modules.shifts.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegisteredShiftDto extends BaseDto {
	private String id;
	private Double shiftPrice;
	private Integer maxNumberOfPatients;
	private Boolean isApproved;

	private ShiftDto shift;
	private DoctorDto doctor;
	private RoomDto room;
}
