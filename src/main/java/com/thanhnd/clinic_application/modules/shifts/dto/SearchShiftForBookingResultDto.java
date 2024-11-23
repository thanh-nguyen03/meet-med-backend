package com.thanhnd.clinic_application.modules.shifts.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchShiftForBookingResultDto {
	private Double shiftPrice;
	private String doctorId;
	private ShiftDto shift;
	private List<RegisteredShiftTimeSlotDto> timeSlots;
}
