package com.thanhnd.clinic_application.modules.rooms.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.rooms.dto.RoomDto;
import com.thanhnd.clinic_application.modules.rooms.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(ControllerPath.ROOM_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class AdminRoomController extends BaseController {
	private final RoomService roomService;

	@GetMapping
	@PermissionsAllowed(permissions = {Permissions.Room.READ})
	public ResponseEntity<ResponseDto> findAll() {
		return createSuccessResponse(ResponseDto.success(roomService.findAll()));
	}

	@GetMapping("/{roomId}")
	@PermissionsAllowed(permissions = {Permissions.Room.READ})
	public ResponseEntity<ResponseDto> findById(@PathVariable String roomId) {
		return createSuccessResponse(ResponseDto.success(roomService.findById(roomId)));
	}

	@PostMapping
	@PermissionsAllowed(permissions = {Permissions.Room.WRITE})
	public ResponseEntity<ResponseDto> create(@RequestBody @Valid RoomDto roomDto) {
		return createSuccessResponse(ResponseDto.success(roomService.createRoom(roomDto)));
	}

	@PutMapping("/{roomId}")
	@PermissionsAllowed(permissions = {Permissions.Room.WRITE})
	public ResponseEntity<ResponseDto> update(
		@PathVariable String roomId,
		@RequestBody @Valid RoomDto roomDto
	) {
		roomDto.setId(roomId);
		return createSuccessResponse(ResponseDto.success(roomService.updateRoom(roomDto)));
	}

	@DeleteMapping
	@PermissionsAllowed(permissions = {Permissions.Room.WRITE})
	public ResponseEntity<ResponseDto> delete(@RequestParam String roomId) {
		roomService.deleteRoom(roomId);
		return createSuccessResponse(ResponseDto.success());
	}
}
