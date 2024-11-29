package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.FcmDeviceToken;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.FcmDeviceTokenDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FcmDeviceTokenMapper extends IBaseMapper<FcmDeviceToken, FcmDeviceTokenDto> {
}
