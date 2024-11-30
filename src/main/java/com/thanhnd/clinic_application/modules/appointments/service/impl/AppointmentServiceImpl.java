package com.thanhnd.clinic_application.modules.appointments.service.impl;

import com.google.gson.JsonObject;
import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.NotificationMessage;
import com.thanhnd.clinic_application.constants.NotificationType;
import com.thanhnd.clinic_application.entity.*;
import com.thanhnd.clinic_application.helper.NotificationHelper;
import com.thanhnd.clinic_application.mapper.AppointmentMapper;
import com.thanhnd.clinic_application.mapper.NotificationMapper;
import com.thanhnd.clinic_application.modules.amqp.dto.AmqpMessage;
import com.thanhnd.clinic_application.modules.amqp.service.AmqpService;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import com.thanhnd.clinic_application.modules.appointments.repository.AppointmentRepository;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentService;
import com.thanhnd.clinic_application.modules.appointments.specification.AppointmentSpecification;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.FcmDeviceTokenDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.service.FcmDeviceTokenService;
import com.thanhnd.clinic_application.modules.notifications.dto.AmqpNotificationMessageDto;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftTimeSlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
	private final AppointmentRepository appointmentRepository;
	private final RegisteredShiftTimeSlotRepository registeredShiftTimeSlotRepository;
	private final RegisteredShiftRepository registeredShiftRepository;
	private final PatientRepository patientRepository;
	private final DoctorRepository doctorRepository;

	private final AppointmentMapper appointmentMapper;
	private final NotificationMapper notificationMapper;

	private final JwtAuthenticationManager jwtAuthenticationManager;
	private final NotificationService notificationService;
	private final AmqpService amqpService;
	private final FcmDeviceTokenService fcmDeviceTokenService;

	@Override
	public PageableResultDto<AppointmentDto> findAllForDoctor(
		Pageable pageable,
		String registeredShiftId,
		AppointmentStatus appointmentStatus
	) {
		List<Specification<Appointment>> specifications = new ArrayList<>();

		Doctor doctor = doctorRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.DOCTOR_NOT_FOUND.getMessage()));

		specifications.add(AppointmentSpecification.ofDoctor(doctor.getId()));

		if (registeredShiftId != null && !registeredShiftId.isEmpty()) {
			RegisteredShift registeredShift = registeredShiftRepository.findById(registeredShiftId)
				.orElseThrow(() -> HttpException.notFound(Message.REGISTERED_SHIFT_NOT_FOUND.getMessage()));

			if (!registeredShift.getDoctor().getId().equals(doctor.getId())) {
				throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
			}

			specifications.add(AppointmentSpecification.inRegisteredShift(registeredShiftId));
		}

		if (appointmentStatus != null) {
			specifications.add(AppointmentSpecification.hasStatus(appointmentStatus));
		}

		Specification<Appointment> specification = specifications
			.stream()
			.reduce(Specification::and)
			.orElse(null);

		Page<Appointment> appointmentPage = appointmentRepository.findAll(specification, pageable);

		return PageableResultDto.parse(appointmentPage.map(appointmentMapper::toDto));
	}

	@Override
	public PageableResultDto<AppointmentDto> findAllByPatientUserId(
		Pageable pageable,
		String userId,
		AppointmentStatus status
	) {
		Specification<Appointment> specification = AppointmentSpecification.ofPatientUserId(userId);

		if (status != null) {
			specification = specification.and(AppointmentSpecification.hasStatus(status));
		}

		Page<Appointment> appointmentPage = appointmentRepository.findAll(specification, pageable);

		return PageableResultDto.parse(appointmentPage.map(appointmentMapper::toDto));
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

		if (!registeredShiftTimeSlot.getRegisteredShift().getIsApproved()) {
			throw HttpException.badRequest(Message.SHIFT_NOT_FOUND.getMessage());
		}

		Patient patient = patientRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.PATIENT_NOT_FOUND.getMessage()));

		List<Appointment> preBookedAppointment = appointmentRepository.findAllByRegisteredShiftTimeSlotId(registeredShiftTimeSlot.getId());

		// Check if the time slot is full
		if (preBookedAppointment.size() >= RegisteredShiftTimeSlot.MAX_NUMBER_OF_PATIENTS) {
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

		AppointmentDto returnedAppointmentDto = appointmentMapper.toDto(appointmentRepository.save(appointment));

		if (preBookedAppointment.size() == RegisteredShiftTimeSlot.MAX_NUMBER_OF_PATIENTS - 1) {
			registeredShiftTimeSlot.setIsAvailable(false);
			registeredShiftTimeSlotRepository.save(registeredShiftTimeSlot);
		}

		// Save notification
		NotificationDto notificationDto = new NotificationDto();
		notificationDto.setTitle(NotificationMessage.APPOINTMENT_BOOKED_TITLE.getMessage());
		notificationDto.setContent(NotificationMessage.APPOINTMENT_BOOKED_MESSAGE.getMessage());
		notificationDto.setType(NotificationType.BOOK_APPOINTMENT_SUCCESS);
		notificationDto.setReceiverId(patient.getUser().getId());
		JsonObject data = new JsonObject();
		data.addProperty("appointmentId", returnedAppointmentDto.getId());
		notificationDto.setObjectData(data.toString());

		List<String> receiverIds = List.of(patient.getUser().getId());
		List<Notification> notifications = notificationService.sendNotifications(receiverIds, notificationDto);

		// Send notifications message
		for (Notification notification : notifications) {
			AmqpNotificationMessageDto notificationMessageDto = new AmqpNotificationMessageDto();
			notificationMessageDto.setNotification(notificationMapper.toDto(notification));
			notificationMessageDto.setRoomName(
				NotificationHelper.getNotificationRoomNameByUserId(notification.getReceiverId())
			);
			notificationMessageDto.setDeviceTokens(
				fcmDeviceTokenService.findAllByUserId(notification.getReceiverId())
					.stream()
					.map(FcmDeviceTokenDto::getToken)
					.toList()
			);
			amqpService.produceMessage(
				AmqpMessage.builder()
					.timestamp(notification.getCreatedAt())
					.content(notificationMessageDto)
					.build()
			);
		}

		return returnedAppointmentDto;
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

	@Override
	public AppointmentDto updateStatus(String appointmentId, AppointmentStatus status) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> HttpException.notFound(Message.APPOINTMENT_NOT_FOUND.getMessage()));

		Doctor doctor = doctorRepository.findByUserId(jwtAuthenticationManager.getUserId())
			.orElseThrow(() -> HttpException.forbidden(Message.PERMISSION_DENIED.getMessage()));

		if (!appointment.getRegisteredShiftTimeSlot().getRegisteredShift().getDoctor().getId().equals(doctor.getId())) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		appointment.setStatus(status);
		return appointmentMapper.toDto(appointmentRepository.save(appointment));
	}
}
