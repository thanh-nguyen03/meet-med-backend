package com.thanhnd.clinic_application.modules.fcm_device_token.service;

import com.thanhnd.clinic_application.modules.fcm_device_token.dto.FcmDeviceTokenDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.request.RegisterFcmDeviceTokenRequestDto;

import java.util.List;

public interface FcmDeviceTokenService {
	FcmDeviceTokenDto registerToken(RegisterFcmDeviceTokenRequestDto registerFcmDeviceTokenRequestDto);

	List<FcmDeviceTokenDto> findAllByUserId(String userId);
}
