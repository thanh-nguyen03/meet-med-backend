package com.thanhnd.clinic_application.modules.users.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import com.thanhnd.clinic_application.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.USER_CONTROLLER)
@RequiredArgsConstructor
public class UserController extends BaseController {
	private final UserService userService;

	@GetMapping("/user-info")
	public ResponseEntity<ResponseDto> getUserInfo() {
		String identityProviderUserId = jwtAuthenticationManager.getIdentityProviderUserId();

		if (identityProviderUserId == null) {
			return createErrorResponse(ResponseDto.unauthorized(Message.UNAUTHORIZED.getMessage()));
		}

		return createSuccessResponse(ResponseDto.success(userService.findByIdentityProviderUserId(identityProviderUserId)));
	}

	@PostMapping
	public ResponseEntity<ResponseDto> createUserInDB(@RequestBody UserDto userDto) {
		return createSuccessResponse(ResponseDto.success(userService.create(userDto)));
	}

	@PutMapping("/profile")
	public ResponseEntity<ResponseDto> updateProfile(@RequestBody UserDto userDto) {
		String userId = jwtAuthenticationManager.getUserId();

		try {
			UserDto user = userService.findById(userId);
			return createSuccessResponse(ResponseDto.success(userService.update(user.getId(), userDto)));
		} catch (HttpException exception) {
			return createErrorResponse(ResponseDto.forbidden(Message.PERMISSION_DENIED.getMessage()));
		}
	}
}
