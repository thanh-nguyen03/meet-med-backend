package com.thanhnd.clinic_application.modules.users.service;

import com.thanhnd.clinic_application.modules.users.dto.CreateUserDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;

import java.util.List;

public interface UserService {
	UserDto findById(String id);

	List<UserDto> findAll();

	UserDto create(CreateUserDto createUserDto);

	UserDto update(String id, UserDto userDto);

	void delete(String id);
}
