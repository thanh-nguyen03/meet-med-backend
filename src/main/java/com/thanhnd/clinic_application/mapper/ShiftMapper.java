package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Shift;
import com.thanhnd.clinic_application.modules.shifts.dto.ShiftDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShiftMapper extends IBaseMapper<Shift, ShiftDto> {
	@Override
	ShiftDto toDto(Shift entity);
}
