package com.thanhnd.clinic_application.modules.users.controller;

import com.thanhnd.clinic_application.annotation.PermissionsAllowed;
import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.Permissions;
import com.thanhnd.clinic_application.modules.identity_providers.IdentityProviderStrategyFactory;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.users.dto.UpdateRoleDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(ControllerPath.USER_ADMIN_CONTROLLER)
public class UserAdminController extends BaseController {
	private final UserService userService;
	private final IdentityProviderStrategyFactory identityProviderStrategyFactory;

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

	@PutMapping("/{id}")
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> update(@PathVariable String id, @RequestBody @Valid UserDto userDto) {
		return createSuccessResponse(ResponseDto.success(userService.update(id, userDto)));
	}

	@DeleteMapping("/{id}")
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> delete(@PathVariable String id) {
		userService.delete(id);
		return createSuccessResponse(ResponseDto.success());
	}

	@PatchMapping("/{id}/reset-password")
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> changePassword(
		@PathVariable String id,
		@RequestParam(required = false, defaultValue = IdentityProviderStrategyFactory.DEFAULT_STRATEGY) String identity_provider
	) {
		IdentityProviderStrategy strategy = identityProviderStrategyFactory.getStrategy(identity_provider);
		Map<String, Object> user = strategy.getUserByEmail(userService.findById(id).getEmail());
		String message = strategy.resetUserPassword(user);
		return createSuccessResponse(ResponseDto.success(message));
	}

	@PatchMapping("/{id}/roles")
	@PermissionsAllowed(Permissions.Users.WRITE)
	public ResponseEntity<ResponseDto> updateRoles(
		@PathVariable String id,
		@RequestBody @Valid UpdateRoleDto updateRoleDto,
		@RequestParam(required = false, defaultValue = IdentityProviderStrategyFactory.DEFAULT_STRATEGY) String identity_provider
	) {
		IdentityProviderStrategy strategy = identityProviderStrategyFactory.getStrategy(identity_provider);
		Map<String, Object> user = strategy.getUserByEmail(userService.findById(id).getEmail());
		String message = strategy.assignRole(user, updateRoleDto.getRole());

		return createSuccessResponse(ResponseDto.success(message));
	}
}
