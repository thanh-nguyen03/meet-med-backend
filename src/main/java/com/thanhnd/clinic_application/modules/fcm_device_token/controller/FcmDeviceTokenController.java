package com.thanhnd.clinic_application.modules.fcm_device_token.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.request.RegisterFcmDeviceTokenRequestDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.service.FcmDeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ControllerPath.FCM_DEVICE_TOKEN_CONTROLLER)
@RequiredArgsConstructor
public class FcmDeviceTokenController extends BaseController {
	private final FcmDeviceTokenService fcmDeviceTokenService;

	@PostMapping
	public ResponseEntity<ResponseDto> registerToken(
		@RequestBody @Valid RegisterFcmDeviceTokenRequestDto registerFcmDeviceTokenRequestDto
	) {
		return createSuccessResponse(ResponseDto.success(fcmDeviceTokenService.registerToken(registerFcmDeviceTokenRequestDto)));
	}
}
