package com.thanhnd.clinic_application.modules.chat.service;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.modules.chat.dto.ChatMessageDto;
import org.springframework.data.domain.Pageable;


public interface ChatMessageService {
	PageableResultDto<ChatMessageDto> findAllByChatBoxId(String chatBoxId, Pageable pageable);
}
