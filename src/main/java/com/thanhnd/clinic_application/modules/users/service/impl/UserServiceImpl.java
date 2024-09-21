package com.thanhnd.clinic_application.modules.users.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.User;
import com.thanhnd.clinic_application.modules.users.dto.CreateUserDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.repository.UserRepository;
import com.thanhnd.clinic_application.modules.users.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;

	@Override
	public UserDto findById(String id) {
		User user = userRepository.findById(id).orElseThrow(() -> HttpException.notFound(Message.USER_NOT_FOUND.getMessage()));

		return modelMapper.map(user, UserDto.class);
	}

	@Override
	public UserDto findByEmail(String email) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> HttpException.notFound(Message.USER_NOT_FOUND.getMessage()));

		return modelMapper.map(user, UserDto.class);
	}

	@Override
	public List<UserDto> findAll() {
		return userRepository.findAll().stream().map((element) -> modelMapper.map(element, UserDto.class)).collect(Collectors.toList());
	}

	@Override
	public UserDto create(CreateUserDto createUserDto) {
		Optional<User> existingUser = userRepository.findByEmail(createUserDto.getEmail());

		if (existingUser.isPresent()) {
			throw HttpException.badRequest(Message.USER_EMAIL_ALREADY_EXISTS.getMessage(createUserDto.getEmail()));
		}

		User user = modelMapper.map(createUserDto, User.class);

		return modelMapper.map(this.userRepository.save(user), UserDto.class);
	}

	@Override
	public UserDto update(String id, UserDto userDto) {
		User existingUser = userRepository.findById(id)
			.orElseThrow(() -> HttpException.badRequest(Message.USER_NOT_FOUND.getMessage()));

		modelMapper.map(userDto, existingUser);

		return modelMapper.map(this.userRepository.save(existingUser), UserDto.class);
	}

	@Override
	public void delete(String id) {
		User ExistingUser = userRepository.findById(id)
			.orElseThrow(() -> HttpException.badRequest(Message.USER_NOT_FOUND.getMessage()));

		userRepository.delete(ExistingUser);
	}
}
