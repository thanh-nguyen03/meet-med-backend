package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.User;
import com.thanhnd.clinic_application.modules.users.dto.CreateUserDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper extends IBaseMapper<User, UserDto> {
	@Override
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "role", ignore = true)
	void merge(@MappingTarget User entity, UserDto dto);

	User create(CreateUserDto createUserDto);
}
