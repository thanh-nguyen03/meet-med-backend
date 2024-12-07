package com.thanhnd.clinic_application.modules.chat.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.constants.PaginationConstants;
import com.thanhnd.clinic_application.modules.chat.service.ChatBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerPath.CHAT_BOX_CONTROLLER)
@RequiredArgsConstructor
public class ChatBoxController extends BaseController {
	private final ChatBoxService chatBoxService;

	@GetMapping
	public ResponseEntity<ResponseDto> finaAllByUser(
		@RequestParam(value = PaginationConstants.PAGE, defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
		@RequestParam(value = PaginationConstants.SIZE, defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "updatedAt", required = false) String orderBy,
		@RequestParam(value = PaginationConstants.ORDER_BY, defaultValue = "desc", required = false) String orderType,
		@RequestParam(value = "doctorName", required = false) String doctorName
	) {
		String userId = jwtAuthenticationManager.getUserId();
		return createSuccessResponse(ResponseDto.success(
			chatBoxService.findAllByUser(parsePageRequest(page, size, orderBy, orderType), userId, doctorName)
		));
	}

	@GetMapping("/{chatBoxId}")
	public ResponseEntity<ResponseDto> findById(@PathVariable String chatBoxId) {
		return createSuccessResponse(ResponseDto.success(chatBoxService.findById(chatBoxId)));
	}
}
