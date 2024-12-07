package com.thanhnd.clinic_application.modules.shifts.service.impl;

import com.google.gson.JsonObject;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.MessageQueueConstants;
import com.thanhnd.clinic_application.constants.NotificationMessage;
import com.thanhnd.clinic_application.constants.NotificationType;
import com.thanhnd.clinic_application.entity.Notification;
import com.thanhnd.clinic_application.entity.RegisteredShift;
import com.thanhnd.clinic_application.helper.DateHelper;
import com.thanhnd.clinic_application.mapper.NotificationMapper;
import com.thanhnd.clinic_application.modules.amqp.dto.AmqpMessage;
import com.thanhnd.clinic_application.modules.amqp.service.AmqpService;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.FcmDeviceTokenDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.service.FcmDeviceTokenService;
import com.thanhnd.clinic_application.modules.notifications.dto.AmqpNotificationMessageDto;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import com.thanhnd.clinic_application.modules.shifts.repository.RegisteredShiftRepository;
import com.thanhnd.clinic_application.modules.shifts.service.RegisteredShiftReminderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisteredShiftReminderServiceImpl implements RegisteredShiftReminderService {
	private final RegisteredShiftRepository registeredShiftRepository;

	private final NotificationMapper notificationMapper;

	private final NotificationService notificationService;
	private final AmqpService amqpService;
	private final FcmDeviceTokenService fcmDeviceTokenService;

	@Override
	@Scheduled(cron = "0 0 20 * * *") // Run every day at 8:00 PM
	public void sendTomorrowShiftReminder() {
		LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0);
		LocalDateTime end = LocalDateTime.now().plusDays(1).withHour(23).withMinute(59);

		Instant startInstant = start.atZone(DateHelper.ZONE_ID).toInstant();
		Instant endInstant = end.atZone(DateHelper.ZONE_ID).toInstant();

		List<RegisteredShift> registeredShifts = registeredShiftRepository.findAllByDateTimeBetween(startInstant, endInstant);

		registeredShifts.forEach(registeredShift -> {
			String message = NotificationMessage.WORKING_SHIFT_REMINDER_TOMORROW_MESSAGE.getMessage();
			NotificationDto notificationDto = new NotificationDto();
			notificationDto.setTitle(NotificationMessage.WORKING_SHIFT_REMINDER_TITLE.getMessage());
			notificationDto.setContent(message);
			notificationDto.setReceiverId(registeredShift.getDoctor().getUser().getId());
			notificationDto.setType(NotificationType.WORKING_SHIFT_REMINDER);
			JsonObject objectData = new JsonObject();
			objectData.addProperty("registeredShiftId", registeredShift.getId());
			notificationDto.setObjectData(objectData.toString());

			List<String> receiverIds = List.of(registeredShift.getDoctor().getUser().getId());
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
					MessageQueueConstants.ExchangeName.NOTIFICATION_EXCHANGE,
					MessageQueueConstants.RoutingKey.NOTIFICATION_ROUTING_KEY,
					AmqpMessage.builder()
						.timestamp(notification.getCreatedAt())
						.content(notificationMessageDto)
						.build()
				);
			}
		});
	}

	@Override
	public void sendMockReminder(String registeredShiftId) {
		RegisteredShift registeredShift = registeredShiftRepository.findById(registeredShiftId)
			.orElseThrow(() -> HttpException.notFound(Message.REGISTERED_SHIFT_NOT_FOUND.getMessage()));

		String message = "Mock Notification - " + NotificationMessage.WORKING_SHIFT_REMINDER_TOMORROW_MESSAGE.getMessage();
		NotificationDto notificationDto = new NotificationDto();
		notificationDto.setTitle(NotificationMessage.WORKING_SHIFT_REMINDER_TITLE.getMessage());
		notificationDto.setContent(message);
		notificationDto.setReceiverId(registeredShift.getDoctor().getUser().getId());
		notificationDto.setType(NotificationType.WORKING_SHIFT_REMINDER);
		JsonObject objectData = new JsonObject();
		objectData.addProperty("registeredShiftId", registeredShift.getId());
		notificationDto.setObjectData(objectData.toString());

		List<String> receiverIds = List.of(registeredShift.getDoctor().getUser().getId());
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
				MessageQueueConstants.ExchangeName.NOTIFICATION_EXCHANGE,
				MessageQueueConstants.RoutingKey.NOTIFICATION_ROUTING_KEY,
				AmqpMessage.builder()
					.timestamp(notification.getCreatedAt())
					.content(notificationMessageDto)
					.build()
			);
		}
	}
}
