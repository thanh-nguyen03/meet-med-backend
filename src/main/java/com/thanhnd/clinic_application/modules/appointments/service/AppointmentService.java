package com.thanhnd.clinic_application.modules.appointments.service;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
	PageableResultDto<AppointmentDto> findAllForDoctor(
		Pageable pageable,
		String registeredShiftId,
		AppointmentStatus appointmentStatus
	);

	PageableResultDto<AppointmentDto> findAllByPatientUserId(Pageable pageable, String userId, AppointmentStatus status);

	AppointmentDto findById(String id);

	AppointmentDto create(AppointmentDto appointmentDto);

	AppointmentDto update(AppointmentDto appointmentDto);

	void delete(String id);

	AppointmentDto updateStatus(String appointmentId, AppointmentStatus status);
}
