package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Appointment;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper extends IBaseMapper<Appointment, AppointmentDto> {
	@Override
	@Mapping(target = "doctor", source = "registeredShiftTimeSlot.registeredShift.doctor")
	@Mapping(target = "doctor.department.doctors", ignore = true)
	@Mapping(target = "doctor.department.headDoctor", ignore = true)
	AppointmentDto toDto(Appointment entity);
}
