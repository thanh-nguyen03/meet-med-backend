package com.thanhnd.clinic_application.modules.notifications.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.entity.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends BaseRepository<Notification, String> {
	Optional<Notification> findByIdAndReceiverId(String id, String receiverId);

	List<Notification> findAllByReceiverIdAndStatusEquals(String receiverId, NotificationStatus status);
}
