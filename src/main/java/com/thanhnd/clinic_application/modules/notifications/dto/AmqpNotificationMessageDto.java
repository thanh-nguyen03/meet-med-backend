package com.thanhnd.clinic_application.modules.notifications.dto;

import lombok.Data;

import java.util.List;

@Data
public class AmqpNotificationMessageDto {
	private String roomName;
	private List<String> deviceTokens;
	private NotificationDto notification;
}
