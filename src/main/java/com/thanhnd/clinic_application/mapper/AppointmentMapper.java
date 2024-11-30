package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Appointment;
import com.thanhnd.clinic_application.entity.Department;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper extends IBaseMapper<Appointment, AppointmentDto> {
	@Override
	@Mapping(target = "doctor", source = "registeredShiftTimeSlot.registeredShift.doctor")
	AppointmentDto toDto(Appointment entity);

	@Mapping(target = "doctors", ignore = true)
	@Mapping(target = "headDoctor", ignore = true)
	DepartmentDto toDepartmentDto(Department department);
}
