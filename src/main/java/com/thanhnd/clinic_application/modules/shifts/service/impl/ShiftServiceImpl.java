package com.thanhnd.clinic_application.modules.shifts.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.*;
import com.thanhnd.clinic_application.helper.DateHelper;
import com.thanhnd.clinic_application.mapper.RegisteredShiftMapper;
import com.thanhnd.clinic_application.mapper.ShiftMapper;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.rooms.repository.RoomRepository;
import com.thanhnd.clinic_application.modules.shifts.dto.CanRegisterShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.ShiftDto;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.ShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.service.ShiftService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {
	public static final int MAX_SHIFT_HOUR = 4;
	public static final int MORNING_START_HOUR = 8;
	public static final int AFTERNOON_START_HOUR = 13;

	private final ShiftRepository shiftRepository;
	private final DoctorRepository doctorRepository;
	private final RegisteredShiftRepository registeredShiftRepository;
	private final RoomRepository roomRepository;

	private final JwtAuthenticationManager jwtAuthenticationManager;

	private final ShiftMapper shiftMapper;
	private final RegisteredShiftMapper registeredShiftMapper;

	@Override
	public List<ShiftDto> getShiftList(LocalDate startDate, LocalDate endDate) {
		Instant start = DateHelper.getStartOfDay(startDate);
		Instant end = DateHelper.getEndOfDay(endDate);

		return shiftRepository.findAllByTimeBetween(start, end)
			.stream()
			.map(shiftMapper::toDto)
			.collect(Collectors.toList());
	}

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

	@Override
	public List<CanRegisterShiftDto> getListShiftCanRegister() {
		String userId = jwtAuthenticationManager.getUserId();

		Doctor doctor = doctorRepository.findByUserId(userId)
			.orElseThrow(() -> HttpException.forbidden(Message.PERMISSION_DENIED.getMessage()));

		// A doctor can register a shift within 1 week from the next day from the current day
		LocalDate nextDay = LocalDate.now().plusDays(1);
		LocalDate nextWeek = nextDay.plusDays(6);

		Instant start = DateHelper.getStartOfDay(nextDay);
		Instant end = DateHelper.getEndOfDay(nextWeek);

		return getListByDoctorIdAndTime(doctor, start, end);
	}

	@Override
	public List<CanRegisterShiftDto> getWeekShiftCanRegister(Boolean isNextWeek) {
		String userId = jwtAuthenticationManager.getUserId();

		Doctor doctor = doctorRepository.findByUserId(userId)
			.orElseThrow(() -> HttpException.forbidden(Message.PERMISSION_DENIED.getMessage()));

		// Get the start and end of the week
		LocalDate currentDay = LocalDate.now();
		if (isNextWeek) {
			currentDay = currentDay.plusWeeks(1);
		}
		LocalDate startOfWeek = DateHelper.getStartOfWeek(currentDay);
		LocalDate endOfWeek = DateHelper.getEndOfWeek(currentDay);

		Instant start = DateHelper.getStartOfDay(startOfWeek);
		Instant end = DateHelper.getEndOfDay(endOfWeek);

		return getListByDoctorIdAndTime(doctor, start, end);
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

	private List<CanRegisterShiftDto> getListByDoctorIdAndTime(Doctor doctor, Instant start, Instant end) {
		List<Shift> shifts = shiftRepository.findAllByTimeBetween(start, end);
		List<RegisteredShift> registeredShifts = registeredShiftRepository.findAllByDoctorIdAndShiftTimeBetween(doctor.getId(), start, end);
		Department department = doctor.getDepartment();
		List<Room> departmentRooms = roomRepository.findAllByDepartmentId(department.getId());

		return shifts.stream()
			.map((shift) -> {
				CanRegisterShiftDto canRegisterShiftDto = new CanRegisterShiftDto(shift);
				// Check if the shift is already registered by the doctor
				RegisteredShift foundRegisteredShift = registeredShifts.stream()
					.filter(registeredShift -> registeredShift.getShift().getId().equals(shift.getId()))
					.findFirst()
					.orElse(null);

				List<RegisteredShift> registeredShiftInShift = registeredShifts.stream()
					.filter(registeredShift -> registeredShift.getShift().getId().equals(shift.getId()))
					.toList();

				Boolean isApproved = foundRegisteredShift != null && foundRegisteredShift.getIsApproved();
				// calculate the remaining number of rooms available (in one shift)
				Integer remainingNumberOfRoomsAvailable = departmentRooms.size() - registeredShiftInShift.size();

				canRegisterShiftDto.setRegisteredShift(foundRegisteredShift != null ? registeredShiftMapper.toDtoExcludeShift(foundRegisteredShift) : null);
				canRegisterShiftDto.setIsApproved(isApproved);
				canRegisterShiftDto.setRemainingNumberOfRoomsAvailable(remainingNumberOfRoomsAvailable);

				return canRegisterShiftDto;
			})
			.collect(Collectors.toList());
	}
}
