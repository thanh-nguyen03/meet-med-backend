package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Patient;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper extends IBaseMapper<Patient, PatientDto> {
	@Override
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void merge(@MappingTarget Patient entity, PatientDto dto);
}
