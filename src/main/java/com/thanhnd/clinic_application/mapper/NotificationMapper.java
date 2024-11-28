package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Notification;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper extends IBaseMapper<Notification, NotificationDto> {
}
