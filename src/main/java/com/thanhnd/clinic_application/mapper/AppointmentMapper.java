package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Appointment;
import com.thanhnd.clinic_application.modules.appointments.dto.AppointmentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppointmentMapper extends IBaseMapper<Appointment, AppointmentDto> {
}
