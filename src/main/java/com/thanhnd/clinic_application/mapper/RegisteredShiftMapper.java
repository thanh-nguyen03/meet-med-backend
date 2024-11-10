package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.entity.RegisteredShift;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisteredShiftMapper extends IBaseMapper<RegisteredShift, RegisteredShiftDto> {
	@Mapping(target = "doctors", ignore = true)
	@Mapping(target = "headDoctor", ignore = true)
	DepartmentDto toDepartmentDto(Department entity);

	@Mapping(target = "shift", ignore = true)
	RegisteredShiftDto toDtoExcludeShift(RegisteredShift entity);
}
