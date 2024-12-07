package com.thanhnd.clinic_application.modules.appointments.service.impl;

import com.google.gson.JsonObject;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.MessageQueueConstants;
import com.thanhnd.clinic_application.constants.NotificationMessage;
import com.thanhnd.clinic_application.constants.NotificationType;
import com.thanhnd.clinic_application.entity.Appointment;
import com.thanhnd.clinic_application.entity.Notification;
import com.thanhnd.clinic_application.helper.DateHelper;
import com.thanhnd.clinic_application.mapper.NotificationMapper;
import com.thanhnd.clinic_application.modules.amqp.dto.AmqpMessage;
import com.thanhnd.clinic_application.modules.amqp.service.AmqpService;
import com.thanhnd.clinic_application.modules.appointments.repository.AppointmentRepository;
import com.thanhnd.clinic_application.modules.appointments.service.AppointmentReminderService;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.FcmDeviceTokenDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.service.FcmDeviceTokenService;
import com.thanhnd.clinic_application.modules.notifications.dto.AmqpNotificationMessageDto;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentReminderServiceImpl implements AppointmentReminderService {
	private final AppointmentRepository appointmentRepository;

	private final AmqpService amqpService;
	private final NotificationService notificationService;
	private final FcmDeviceTokenService fcmDeviceTokenService;

	private final NotificationMapper notificationMapper;

	@Override
	public void send24HourReminder() {
		Instant now = Instant.now();
		Instant next24Hour = Instant.now().plusSeconds(24 * 60 * 60);

		List<Appointment> appointments = appointmentRepository.findAllByDateTimeBetweenAAndIs24HourNotificationSentFalse(now, next24Hour);

		appointments.forEach(appointment -> {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
			LocalDateTime appointmentStartTime = appointment
				.getRegisteredShiftTimeSlot()
				.getStartTime()
				.atZone(DateHelper.ZONE_ID)
				.toLocalDateTime();

			String message = NotificationMessage.APPOINTMENT_REMINDER_24_HOUR_MESSAGE.getMessage(
				appointment
					.getRegisteredShiftTimeSlot()
					.getRegisteredShift()
					.getDoctor()
					.getUser()
					.getFullName(),
				appointmentStartTime.format(formatter)
			);
			sendNotification(appointment, message);
			appointment.setIs24HourNotificationSent(true);
			appointmentRepository.save(appointment);
		});
	}

	@Override
	@Scheduled(cron = "0 * * * * *") // Run every minute
	public void send1HourReminder() {
		Instant now = Instant.now();
		Instant next1Hour = Instant.now().plusSeconds(60 * 60);

		List<Appointment> appointments = appointmentRepository.findAllByDateTimeBetweenAndIs1HourNotificationSentFalse(now, next1Hour);

		appointments.forEach(appointment -> {
			String message = NotificationMessage.APPOINTMENT_REMINDER_1_HOUR_MESSAGE.getMessage(
				appointment
					.getRegisteredShiftTimeSlot()
					.getRegisteredShift()
					.getDoctor()
					.getUser()
					.getFullName()
			);
			if (appointment.getIs1HourNotificationSent()) {
				return;
			}
			sendNotification(appointment, message);
			appointment.setIs1HourNotificationSent(true);
			appointmentRepository.save(appointment);
			log.info("Sent 1 hour reminder for appointment with id: {}", appointment.getId());
		});
	}

	@Override
	public void sendMockReminder(String appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> HttpException.notFound(Message.APPOINTMENT_NOT_FOUND.getMessage()));

		String message = "Mock Notification - " + NotificationMessage.APPOINTMENT_REMINDER_1_HOUR_MESSAGE.getMessage(
			appointment
				.getRegisteredShiftTimeSlot()
				.getRegisteredShift()
				.getDoctor()
				.getUser()
				.getFullName()
		);

		sendNotification(appointment, message);
		log.info("Sent mock reminder for appointment with id: {}", appointment.getId());
	}

	private void sendNotification(Appointment appointment, String message) {
		NotificationDto notificationDto = new NotificationDto();
		notificationDto.setTitle(NotificationMessage.APPOINTMENT_REMINDER_TITLE.getMessage());
		notificationDto.setContent(message);
		notificationDto.setReceiverId(appointment.getPatient().getUser().getId());
		notificationDto.setType(NotificationType.APPOINTMENT_REMINDER);
		JsonObject objectData = new JsonObject();
		objectData.addProperty("appointmentId", appointment.getId());
		notificationDto.setObjectData(objectData.toString());

		List<String> receiverIds = List.of(appointment.getPatient().getUser().getId());
		List<Notification> notifications = notificationService.sendNotifications(receiverIds, notificationDto);

		for (Notification notification : notifications) {
			List<String> deviceTokens = fcmDeviceTokenService.findAllByUserId(notification.getReceiverId())
				.stream()
				.map(FcmDeviceTokenDto::getToken)
				.toList();

			if (deviceTokens.isEmpty()) {
				continue;
			}

			AmqpNotificationMessageDto notificationMessageDto = new AmqpNotificationMessageDto();
			notificationMessageDto.setNotification(notificationMapper.toDto(notification));
			notificationMessageDto.setDeviceTokens(deviceTokens);

			amqpService.produceMessage(
				MessageQueueConstants.QueueName.NOTIFICATION_QUEUE,
				MessageQueueConstants.RoutingKey.NOTIFICATION_ROUTING_KEY,
				AmqpMessage.builder()
					.timestamp(notification.getCreatedAt())
					.content(notificationMessageDto)
					.build()
			);
		}
	}
}
