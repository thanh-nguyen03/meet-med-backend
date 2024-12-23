package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.ChatBox;
import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatBoxMapper extends IBaseMapper<ChatBox, ChatBoxDto> {
	@Mapping(target = "doctors", ignore = true)
	@Mapping(target = "headDoctor", ignore = true)
	DepartmentDto toDepartmentDto(Department chatBox);
}
