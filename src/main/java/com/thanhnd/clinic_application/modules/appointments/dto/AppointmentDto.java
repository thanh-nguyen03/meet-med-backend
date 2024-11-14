package com.thanhnd.clinic_application.modules.appointments.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.ValidationMessage;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftTimeSlotDto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppointmentDto extends BaseDto {
	private String id;
	private String symptoms;

	@NotNull(message = ValidationMessage.TIME_SLOT_REQUIRED)
	private RegisteredShiftTimeSlotDto registeredShiftTimeSlot;
	private PatientDto patient;
}
