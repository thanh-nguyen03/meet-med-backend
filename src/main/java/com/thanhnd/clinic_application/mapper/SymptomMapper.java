package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.entity.Symptom;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.symptom.dto.SymptomDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SymptomMapper extends IBaseMapper<Symptom, SymptomDto> {
	@Mapping(target = "doctors", ignore = true)
	@Mapping(target = "headDoctor", ignore = true)
	DepartmentDto toDepartmentDto(Department department);
}
