package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartmentMapper extends IBaseMapper<Department, DepartmentDto> {
	@Override
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "name", expression = "java(com.thanhnd.clinic_application.helper.StringHelper.toTitleCase(dto.getName()))")
	@Mapping(target = "doctors", ignore = true)
	void merge(@MappingTarget Department entity, DepartmentDto dto);
}
