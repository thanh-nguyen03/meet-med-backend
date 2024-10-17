package com.thanhnd.clinic_application.modules.rooms.service;

import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;

import java.util.List;

public interface RoomService {
	List<RoomDto> findAll();

	RoomDto findById(String id);

	RoomDto createRoom(RoomDto roomDto);

	RoomDto updateRoom(RoomDto roomDto);

	void deleteRoom(String id);
}
