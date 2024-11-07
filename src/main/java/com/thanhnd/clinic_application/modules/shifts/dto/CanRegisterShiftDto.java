package com.thanhnd.clinic_application.modules.shifts.dto;


import com.thanhnd.clinic_application.entity.Shift;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CanRegisterShiftDto extends ShiftDto {
	public CanRegisterShiftDto(Shift shift) {
		super();
		this.setId(shift.getId());
		this.setStartTime(shift.getStartTime());
		this.setEndTime(shift.getEndTime());
		this.setCreatedAt(shift.getCreatedAt());
		this.setUpdatedAt(shift.getUpdatedAt());
		this.isRegisteredByCurrentDoctor = false;
		this.isApproved = false;
	}

	private Integer remainingNumberOfRoomsAvailable;
	private Boolean isRegisteredByCurrentDoctor;
	private Boolean isApproved;
}
