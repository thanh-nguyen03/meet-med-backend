package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.Room;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper extends IBaseMapper<Room, RoomDto> {
	@Override
	@Mapping(target = "id", ignore = true)
	void merge(Room entity, RoomDto dto);
}
