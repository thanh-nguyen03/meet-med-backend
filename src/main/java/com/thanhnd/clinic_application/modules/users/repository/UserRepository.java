package com.thanhnd.clinic_application.modules.users.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, String> {
	Optional<User> findByEmail(String email);

	Optional<User> findByIdentityProviderId(String identityProviderId);
}
