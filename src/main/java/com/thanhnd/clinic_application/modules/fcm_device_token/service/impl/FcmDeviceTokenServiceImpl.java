package com.thanhnd.clinic_application.modules.fcm_device_token.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.FcmDeviceToken;
import com.thanhnd.clinic_application.entity.User;
import com.thanhnd.clinic_application.mapper.FcmDeviceTokenMapper;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.FcmDeviceTokenDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.dto.request.RegisterFcmDeviceTokenRequestDto;
import com.thanhnd.clinic_application.modules.fcm_device_token.repository.FcmDeviceTokenRepository;
import com.thanhnd.clinic_application.modules.fcm_device_token.service.FcmDeviceTokenService;
import com.thanhnd.clinic_application.modules.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmDeviceTokenServiceImpl implements FcmDeviceTokenService {
	private final FcmDeviceTokenRepository fcmDeviceTokenRepository;
	private final UserRepository userRepository;

	private final FcmDeviceTokenMapper fcmDeviceTokenMapper;

	@Override
	public FcmDeviceTokenDto registerToken(RegisterFcmDeviceTokenRequestDto registerFcmDeviceTokenRequestDto) {
		User user = userRepository.findById(registerFcmDeviceTokenRequestDto.getUserId())
			.orElseThrow(() -> HttpException.notFound(Message.USER_NOT_FOUND.getMessage()));

		FcmDeviceToken fcmDeviceToken = fcmDeviceTokenRepository.findByToken(registerFcmDeviceTokenRequestDto.getToken())
			.orElse(null);

		if (fcmDeviceToken != null) {
			fcmDeviceToken.setUser(user);
			return fcmDeviceTokenMapper.toDto(fcmDeviceTokenRepository.save(fcmDeviceToken));
		} else {
			FcmDeviceToken newFcmDeviceToken = new FcmDeviceToken();
			newFcmDeviceToken.setToken(registerFcmDeviceTokenRequestDto.getToken());
			newFcmDeviceToken.setUser(user);
			return fcmDeviceTokenMapper.toDto(fcmDeviceTokenRepository.save(newFcmDeviceToken));
		}
	}

	@Override
	public List<FcmDeviceTokenDto> findAllByUserId(String userId) {
		return fcmDeviceTokenRepository.findAllByUserId(userId).stream()
			.map(fcmDeviceTokenMapper::toDto)
			.toList();
	}
}
