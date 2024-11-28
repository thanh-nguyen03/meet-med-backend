package com.thanhnd.clinic_application.modules.notifications.service.impl;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.entity.Notification;
import com.thanhnd.clinic_application.helper.NotificationHelper;
import com.thanhnd.clinic_application.mapper.NotificationMapper;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import com.thanhnd.clinic_application.modules.notifications.repository.NotificationRepository;
import com.thanhnd.clinic_application.modules.notifications.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;

	private final NotificationMapper notificationMapper;

	@Override
	public List<Notification> sendNotifications(List<String> receiverIds, NotificationDto notificationDto) {
		List<Notification> notifications = receiverIds.stream()
			.map(receiverId -> {
				Notification notification = notificationMapper.toEntity(notificationDto);
				notification.setReceiverId(receiverId);
				return notification;
			})
			.toList();

		return notificationRepository.saveAll(notifications);
	}

	@Override
	public PageableResultDto<NotificationDto> getNotifications(String receiverId, Pageable pageable) {
		return PageableResultDto.parse(notificationRepository.findAll(pageable).map(notificationMapper::toDto));
	}

	@Override
	public NotificationDto getNotification(String receiverId, String id) {
		Notification notification = notificationRepository.findById(id)
			.orElseThrow(() -> HttpException.notFound(Message.NOTIFICATION_NOT_FOUND.getMessage()));

		return notificationMapper.toDto(notification);
	}

	@Override
	public void updateStatus(String receiverId, String id, NotificationStatus status) {
		Notification notification = notificationRepository.findByIdAndReceiverId(id, receiverId)
			.orElseThrow(() -> HttpException.notFound(Message.NOTIFICATION_NOT_FOUND.getMessage()));

		notification.setStatus(status);
		notificationRepository.save(notification);
	}

	@Override
	public void updateAllStatus(String receiverId, NotificationStatus status) {
		List<Notification> notifications = notificationRepository.findAllByReceiverIdAndStatusEquals(receiverId, NotificationStatus.OPEN);
		notifications.forEach(notification -> notification.setStatus(status));
		notificationRepository.saveAll(notifications);
	}

	@Override
	public List<String> getSocketIONameRooms(String userId) {
		List<String> rooms = new ArrayList<>();
		rooms.add(NotificationHelper.getNotificationRoomNameByUserId(userId));
		return rooms;
	}
}
