package com.thanhnd.clinic_application.modules.shifts.service;

import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.SearchShiftForBookingResultDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.ApproveRegisteredShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.RegisterShiftRequestDto;

import java.util.List;

public interface RegisteredShiftService {
	List<SearchShiftForBookingResultDto> findAllByDoctorForBooking(String doctorId);

	RegisteredShiftDto findById(String registeredShiftId);

	List<RegisteredShiftDto> create(List<RegisterShiftRequestDto> registerShiftRequestDtoList);

	RegisteredShiftDto approvedRegisteredShift(
		String registeredShiftId,
		ApproveRegisteredShiftRequestDto approveRegisteredShiftRequestDto
	);

	void delete(String registeredShiftId);
}
