package com.thanhnd.clinic_application.modules.notifications.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends BaseRepository<Notification, String> {
	Page<Notification> findAllByReceiverId(String receiverId, Pageable pageable);

	Optional<Notification> findByIdAndReceiverId(String id, String receiverId);

	List<Notification> findAllByReceiverIdAndStatusEquals(String receiverId, NotificationStatus status);
}
