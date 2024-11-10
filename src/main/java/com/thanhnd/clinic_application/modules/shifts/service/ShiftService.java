package com.thanhnd.clinic_application.modules.shifts.service;


import com.thanhnd.clinic_application.modules.shifts.dto.CanRegisterShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.ShiftDto;

import java.time.LocalDate;
import java.util.List;

public interface ShiftService {
	List<ShiftDto> getShiftList(LocalDate startDate, LocalDate endDate);

	void createShiftTable(int month, int year);

	List<CanRegisterShiftDto> getListShiftCanRegister();

	List<CanRegisterShiftDto> getWeekShiftCanRegister(Boolean isNextWeek);
}
