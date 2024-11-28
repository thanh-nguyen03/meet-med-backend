package com.thanhnd.clinic_application.modules.notifications.dto;

import lombok.Data;

@Data
public class AmqpNotificationMessageDto {
	private String roomName;
	private NotificationDto notification;
}
