package com.thanhnd.clinic_application.modules.users.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.constants.Permissions;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.modules.users.dto.CreateUserDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class UserAdminController extends BaseController {
	private final UserService userService;

	@GetMapping
	@PermissionsAllowed(Permissions.Users.READ)
	public ResponseEntity<ResponseDto> findAll() {
		return createSuccessResponse(ResponseDto.success(userService.findAll()));
	}

	@GetMapping("/{id}")
	@PermissionsAllowed(Permissions.Users.READ)
	public ResponseEntity<ResponseDto> findById(@PathVariable String id) {
		return createSuccessResponse(ResponseDto.success(userService.findById(id)));
	}

	@PostMapping
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> create(@RequestBody CreateUserDto createUserDto) {
		return createSuccessResponse(ResponseDto.success(userService.create(createUserDto)));
	}

	@PutMapping("/{id}")
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> update(@PathVariable String id, @RequestBody UserDto userDto) {
		return createSuccessResponse(ResponseDto.success(userService.update(id, userDto)));
	}

	@DeleteMapping("/{id}")
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> delete(@PathVariable String id) {
		userService.delete(id);
		return createSuccessResponse(ResponseDto.success());
	}
}
