package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DoctorMapper extends IBaseMapper<Doctor, DoctorDto> {
	@Override
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", ignore = true)
	void merge(@MappingTarget Doctor entity, DoctorDto dto);

	@Mapping(target = "doctors", ignore = true)
	DepartmentDto toDepartmentDto(Department department);
}
