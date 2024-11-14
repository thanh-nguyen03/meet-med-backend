package com.thanhnd.clinic_application.modules.appointments.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.*;
import com.thanhnd.clinic_application.mapper.AppointmentMapper;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import com.thanhnd.clinic_application.modules.appointments.repository.AppointmentRepository;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentService;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftTimeSlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
	private final AppointmentRepository appointmentRepository;
	private final RegisteredShiftTimeSlotRepository registeredShiftTimeSlotRepository;
	private final RegisteredShiftRepository registeredShiftRepository;
	private final JwtAuthenticationManager jwtAuthenticationManager;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;

	private final AppointmentMapper appointmentMapper;

	@Override
	public List<AppointmentDto> findAllInRegisteredShift(String registeredShiftId) {
		Doctor doctor = doctorRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.DOCTOR_NOT_FOUND.getMessage()));

		RegisteredShift registeredShift = registeredShiftRepository.findById(registeredShiftId)
			.orElseThrow(() -> HttpException.notFound(Message.REGISTERED_SHIFT_NOT_FOUND.getMessage()));

		if (!registeredShift.getDoctor().getId().equals(doctor.getId())) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		return appointmentRepository.findAllByRegisteredShiftId(registeredShiftId)
			.stream()
			.map(appointmentMapper::toDto)
			.toList();
	}

	@Override
	public List<AppointmentDto> findAllByUserId(String userId) {
		return appointmentRepository.findAllByUserId(userId)
			.stream()
			.map(appointmentMapper::toDto)
			.toList();
	}

	@Override
	public AppointmentDto findById(String id) {
		Patient patient = patientRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.PATIENT_NOT_FOUND.getMessage()));

		Appointment appointment = appointmentRepository.findById(id)
			.orElseThrow(() -> HttpException.notFound(Message.APPOINTMENT_NOT_FOUND.getMessage()));

		if (!appointment.getPatient().getId().equals(patient.getId())) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		return appointmentMapper.toDto(appointment);
	}

	@Override
	public AppointmentDto create(AppointmentDto appointmentDto) {
		RegisteredShiftTimeSlot registeredShiftTimeSlot = registeredShiftTimeSlotRepository
			.findById(appointmentDto.getRegisteredShiftTimeSlot().getId())
			.orElseThrow(() -> HttpException.notFound(Message.TIME_SLOT_NOT_FOUND.getMessage()));

		Patient patient = patientRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.PATIENT_NOT_FOUND.getMessage()));

		List<Appointment> preBookedAppointment = appointmentRepository.findAllByRegisteredShiftTimeSlotId(registeredShiftTimeSlot.getId());

		// Check if the time slot is full
		if (preBookedAppointment.size() > RegisteredShiftTimeSlot.MAX_NUMBER_OF_PATIENTS) {
			throw HttpException.badRequest(Message.TIME_SLOT_FULL.getMessage());
		}

		// Check if the patient has already booked the time slot
		if (preBookedAppointment.stream().anyMatch(appointment -> appointment.getPatient().getId().equals(patient.getId()))) {
			throw HttpException.badRequest(Message.TIME_SLOT_ALREADY_BOOKED.getMessage());
		}

		Appointment appointment = new Appointment();
		appointment.setRegisteredShiftTimeSlot(registeredShiftTimeSlot);
		appointment.setPatient(patient);
		appointment.setSymptoms(appointmentDto.getSymptoms());

		return appointmentMapper.toDto(appointmentRepository.save(appointment));
	}

	@Override
	public AppointmentDto update(AppointmentDto appointmentDto) {
		Appointment appointment = appointmentRepository.findById(appointmentDto.getId())
			.orElseThrow(() -> HttpException.notFound(Message.APPOINTMENT_NOT_FOUND.getMessage()));

		RegisteredShiftTimeSlot registeredShiftTimeSlot = registeredShiftTimeSlotRepository
			.findById(appointmentDto.getRegisteredShiftTimeSlot().getId())
			.orElseThrow(() -> HttpException.notFound(Message.TIME_SLOT_NOT_FOUND.getMessage()));

		Patient patient = patientRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.PATIENT_NOT_FOUND.getMessage()));

		List<Appointment> preBookedAppointment = appointmentRepository
			.findAllByRegisteredShiftTimeSlotId(registeredShiftTimeSlot.getId());

		// Check if the time slot is full
		if (preBookedAppointment.size() > RegisteredShiftTimeSlot.MAX_NUMBER_OF_PATIENTS) {
			throw HttpException.badRequest(Message.TIME_SLOT_FULL.getMessage());
		}

		// Check if the patient has already booked the time slot
		if (preBookedAppointment.stream().anyMatch(item -> item.getPatient().getId().equals(patient.getId()))) {
			throw HttpException.badRequest(Message.TIME_SLOT_ALREADY_BOOKED.getMessage());
		}

		appointment.setRegisteredShiftTimeSlot(registeredShiftTimeSlot);
		appointment.setPatient(patient);
		appointment.setSymptoms(appointmentDto.getSymptoms());

		return appointmentMapper.toDto(appointmentRepository.save(appointment));
	}

	@Override
	public void delete(String id) {
		Appointment appointment = appointmentRepository.findById(id)
			.orElseThrow(() -> HttpException.notFound(Message.APPOINTMENT_NOT_FOUND.getMessage()));

		appointmentRepository.delete(appointment);
	}
}
