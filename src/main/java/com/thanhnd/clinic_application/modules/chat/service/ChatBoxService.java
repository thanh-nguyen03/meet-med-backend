package com.thanhnd.clinic_application.modules.chat.service;


import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import org.springframework.data.domain.Pageable;

public interface ChatBoxService {
	PageableResultDto<ChatBoxDto> findAllByUser(Pageable pageable, String userId, String doctorName);

	ChatBoxDto findById(String id);
}
