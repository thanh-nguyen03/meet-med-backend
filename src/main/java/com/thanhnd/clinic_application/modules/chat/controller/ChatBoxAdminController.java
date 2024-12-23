package com.thanhnd.clinic_application.modules.chat.controller;

import com.thanhnd.clinic_application.common.controller.BaseController;
import com.thanhnd.clinic_application.common.dto.ResponseDto;
import com.thanhnd.clinic_application.constants.ControllerPath;
import com.thanhnd.clinic_application.modules.chat.service.ChatBoxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ControllerPath.CHAT_BOX_ADMIN_CONTROLLER)
@RequiredArgsConstructor
public class ChatBoxAdminController extends BaseController {
	private final ChatBoxService chatBoxService;

	@PostMapping("/create-all")
	public ResponseEntity<ResponseDto> createAllChatBox() {
		chatBoxService.createAllChatBox();
		return createSuccessResponse(ResponseDto.success());
	}
}
