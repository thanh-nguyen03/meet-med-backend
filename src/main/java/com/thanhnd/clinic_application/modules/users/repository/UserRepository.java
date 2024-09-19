package com.thanhnd.clinic_application.modules.users.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, String> {
	Optional<User> findByEmail(String email);
}
