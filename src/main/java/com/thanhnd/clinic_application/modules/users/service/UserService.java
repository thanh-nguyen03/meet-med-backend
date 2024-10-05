package com.thanhnd.clinic_application.modules.users.service;

import com.thanhnd.clinic_application.modules.users.dto.UserDto;

import java.util.List;

public interface UserService {
	UserDto findById(String id);

	UserDto findByEmail(String email);

	List<UserDto> findAll();

	UserDto update(String id, UserDto userDto);

	void delete(String id);
}
