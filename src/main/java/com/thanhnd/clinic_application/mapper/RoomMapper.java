package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.entity.Room;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper extends IBaseMapper<Room, RoomDto> {
	@Override
	@Mapping(target = "id", ignore = true)
	void merge(@MappingTarget Room entity, RoomDto dto);

	@Mapping(target = "doctors", ignore = true)
	DepartmentDto toDepartmentDto(Department entity);

	@Mapping(target = "department", ignore = true)
	DoctorDto toDoctorDto(Doctor entity);
}
