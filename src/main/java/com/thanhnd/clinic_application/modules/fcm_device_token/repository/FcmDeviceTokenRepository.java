package com.thanhnd.clinic_application.modules.fcm_device_token.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.FcmDeviceToken;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmDeviceTokenRepository extends BaseRepository<FcmDeviceToken, String> {
	Optional<FcmDeviceToken> findByToken(String token);

	List<FcmDeviceToken> findAllByUserId(String userId);

	void deleteAllByUserId(String userId);
}
