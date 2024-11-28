package com.thanhnd.clinic_application.modules.notifications.service;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.entity.Notification;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
	List<Notification> sendNotifications(List<String> receiverIds, NotificationDto notificationDto);

	PageableResultDto<NotificationDto> getNotifications(String receiverId, Pageable pageable);

	NotificationDto getNotification(String receiverId, String id);

	void updateStatus(String receiverId, String id, NotificationStatus status);

	void updateAllStatus(String receiverId, NotificationStatus status);

	List<String> getSocketIONameRooms(String userId);
}
