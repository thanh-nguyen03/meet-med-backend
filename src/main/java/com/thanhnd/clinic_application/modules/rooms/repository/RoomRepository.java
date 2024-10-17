package com.thanhnd.clinic_application.modules.rooms.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Room;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends BaseRepository<Room, String> {
	Optional<Room> findRoomByNameAndDepartmentId(String name, String departmentId);
}
