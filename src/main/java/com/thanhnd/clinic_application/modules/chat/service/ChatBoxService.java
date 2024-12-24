package com.thanhnd.clinic_application.modules.chat.service;


import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxExtendDto;
import org.springframework.data.domain.Pageable;

public interface ChatBoxService {
	void createAllChatBox();

	PageableResultDto<ChatBoxExtendDto> findAllByUser(Pageable pageable, String userId, String name);

	ChatBoxDto findById(String id);
}
