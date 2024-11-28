package com.thanhnd.clinic_application.entity;

import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.constants.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_notification")
public class Notification extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private String receiverId;

	private String objectData;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationStatus status = NotificationStatus.OPEN;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationType type;
}
