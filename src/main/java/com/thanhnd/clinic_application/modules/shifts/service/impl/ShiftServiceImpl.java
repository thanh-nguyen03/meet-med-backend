package com.thanhnd.clinic_application.modules.shifts.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.Shift;
import com.thanhnd.clinic_application.helper.DateHelper;
import com.thanhnd.clinic_application.modules.shifts.repository.ShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.service.ShiftService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {
	public static final int MAX_SHIFT_HOUR = 4;
	public static final int MORNING_START_HOUR = 8;
	public static final int AFTERNOON_START_HOUR = 13;

	private final ShiftRepository shiftRepository;

	@Override
	public void createShiftTable(int month, int year) {
		Instant firstDayOfMonth = DateHelper.getFirstDayOfMonth(month, year);
		Instant lastDayOfMonth = DateHelper.getLastDayOfMonth(month, year);
		boolean isCreatedBefore = shiftRepository.existsShiftByStartTimeBetween(firstDayOfMonth, lastDayOfMonth);

		if (isCreatedBefore) {
			throw HttpException.badRequest(Message.SHIFT_FOR_MONTH_ALREADY_CREATED.getMessage(month, year));
		}

		LocalDateTime firstDayInCurrentZone = LocalDateTime.ofInstant(firstDayOfMonth, DateHelper.ZONE_ID);
		LocalDateTime lastDayInCurrent = LocalDateTime.ofInstant(lastDayOfMonth, DateHelper.ZONE_ID);

		// Create shift table for all days in month
		for (LocalDateTime date = firstDayInCurrentZone; date.isBefore(lastDayInCurrent); date = date.plusDays(1)) {
			createShiftTableForDay(date);
		}
	}

	private void createShiftTableForDay(LocalDateTime date) {
		LocalDateTime morningShiftStart = date.withHour(MORNING_START_HOUR);
		LocalDateTime afternoonShiftStart = date.withHour(AFTERNOON_START_HOUR);

		Instant morningShiftStartInstant = morningShiftStart.atZone(DateHelper.ZONE_ID).toInstant();
		Instant afternoonShiftStartInstant = afternoonShiftStart.atZone(DateHelper.ZONE_ID).toInstant();

		// Create morning shift
		createShift(morningShiftStartInstant, morningShiftStartInstant.plusSeconds(MAX_SHIFT_HOUR * 3600));

		// Create afternoon shift
		createShift(afternoonShiftStartInstant, afternoonShiftStartInstant.plusSeconds(MAX_SHIFT_HOUR * 3600));
	}

	private void createShift(Instant startTime, Instant endTime) {
		Shift shift = new Shift();
		shift.setStartTime(startTime);
		shift.setEndTime(endTime);

		shiftRepository.save(shift);
	}
}
