package com.thanhnd.clinic_application.modules.notifications.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.NotificationStatus;
import com.thanhnd.clinic_application.constants.NotificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationDto extends BaseDto {
	private String id;
	private String title;
	private String content;
	private String receiverId;
	private String objectData;
	private NotificationStatus status = NotificationStatus.OPEN;
	private NotificationType type;
}
