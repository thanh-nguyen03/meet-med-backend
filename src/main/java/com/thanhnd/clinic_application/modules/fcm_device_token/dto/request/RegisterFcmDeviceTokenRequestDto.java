package com.thanhnd.clinic_application.modules.fcm_device_token.dto.request;

import lombok.Data;

@Data
public class RegisterFcmDeviceTokenRequestDto {
	private String token;
	private String userId;
}
