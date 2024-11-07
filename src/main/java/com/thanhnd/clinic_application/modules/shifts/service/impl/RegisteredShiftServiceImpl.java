package com.thanhnd.clinic_application.modules.shifts.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.entity.RegisteredShift;
import com.thanhnd.clinic_application.entity.Room;
import com.thanhnd.clinic_application.entity.Shift;
import com.thanhnd.clinic_application.mapper.RegisteredShiftMapper;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.doctors.service.DoctorService;
import com.thanhnd.clinic_application.modules.rooms.repository.RoomRepository;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftDto;
import com.thanhnd.clinic_application.modules.shifts.dto.request.RegisterShiftRequestDto;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.ShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisteredShiftServiceImpl implements RegisteredShiftService {
	private static final Integer MAX_NUMBER_OF_PATIENTS_PER_SHIFT = 10;
	private final RegisteredShiftRepository registeredShiftRepository;
	private final DoctorRepository doctorRepository;
	private final RoomRepository roomRepository;
	private final ShiftRepository shiftRepository;

	private final DoctorService doctorService;
	private final JwtAuthenticationManager jwtAuthenticationManager;

	private final RegisteredShiftMapper registeredShiftMapper;

	@Override
	public RegisteredShiftDto findById(String registeredShiftId) {
		return registeredShiftRepository.findById(registeredShiftId)
			.map(registeredShiftMapper::toDto)
			.orElseThrow(() -> HttpException.notFound(Message.REGISTERED_SHIFT_NOT_FOUND.getMessage()));
	}

	@Override
	public List<RegisteredShiftDto> create(List<RegisterShiftRequestDto> registerShiftRequestDtoList) {
		String authenticatedDoctorId = jwtAuthenticationManager.getIdentityProviderId();

		Doctor doctor = doctorRepository.findByUserIdentityProviderId(authenticatedDoctorId)
			.orElseThrow(() -> HttpException.forbidden(Message.PERMISSION_DENIED.getMessage()));

		Set<String> shiftIds = registerShiftRequestDtoList.stream()
			.map(RegisterShiftRequestDto::getId)
			.collect(Collectors.toSet());

		// Check if there are duplicate shifts in the request list
		if (shiftIds.size() != registerShiftRequestDtoList.size()) {
			throw HttpException.badRequest(Message.DUPLICATE_SHIFTS_IN_REQUEST.getMessage());
		}

		List<RegisteredShift> registeredShifts = registerShiftRequestDtoList.stream()
			.map(registerShiftRequestDto -> {
				Shift shift = shiftRepository.findById(registerShiftRequestDto.getId())
					.orElseThrow(() -> HttpException.badRequest(Message.SHIFT_NOT_FOUND.getMessage()));

				return handleRegisterShift(doctor, shift);
			})
			.toList();

		return registeredShifts.stream()
			.map(registeredShiftMapper::toDto)
			.collect(Collectors.toList());
	}

	@Override
	public RegisteredShiftDto update(RegisteredShiftDto registeredShiftDto) {
		return null;
	}

	@Override
	public void delete(String registeredShiftId) {
		RegisteredShift registeredShift = registeredShiftRepository.findById(registeredShiftId)
			.orElseThrow(() -> HttpException.notFound(Message.REGISTERED_SHIFT_NOT_FOUND.getMessage()));

		registeredShiftRepository.delete(registeredShift);
	}

	private RegisteredShift handleRegisterShift(Doctor doctor, Shift shift) {
		// Check if the doctor is already registered for the shift
		if (registeredShiftRepository.findByShiftIdAndDoctorId(shift.getId(), doctor.getId()).isPresent()) {
			throw HttpException.badRequest(Message.DOCTOR_ALREADY_REGISTERED_FOR_SHIFT.getMessage());
		}

		List<Room> departmentRooms = roomRepository.findAllByDepartmentId(doctor.getDepartment().getId());
		List<RegisteredShift> registeredShifts = registeredShiftRepository
			.findAllByShiftIdAndDepartmentId(shift.getId(), doctor.getDepartment().getId());

		// Check if there are any rooms left in the department
		if (departmentRooms.size() == registeredShifts.size()) {
			throw HttpException.badRequest(Message.SHIFT_FULL.getMessage());
		}

		RegisteredShift registeredShift = new RegisteredShift();
		registeredShift.setMaxNumberOfPatients(MAX_NUMBER_OF_PATIENTS_PER_SHIFT);
		registeredShift.setShiftPrice(doctorService.calculateDoctorShiftPrice(doctor));
		registeredShift.setShift(shift);
		registeredShift.setDoctor(doctor);

		return registeredShiftRepository.save(registeredShift);
	}
}
