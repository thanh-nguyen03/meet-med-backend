package com.thanhnd.clinic_application.helper;

public class NotificationHelper {
	public static String getNotificationRoomNameByUserId(String userId) {
		return "NOTIFICATION_" + userId;
	}
}
