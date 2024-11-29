package com.thanhnd.clinic_application.modules.fcm_device_token.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FcmDeviceTokenDto extends BaseDto {
	private String id;
	private String token;
	private UserDto user;
}
