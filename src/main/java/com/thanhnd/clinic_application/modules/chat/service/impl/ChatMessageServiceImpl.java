package com.thanhnd.clinic_application.modules.chat.service.impl;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.entity.ChatMessage;
import com.thanhnd.clinic_application.mapper.ChatMessageMapper;
import com.thanhnd.clinic_application.modules.chat.dto.ChatMessageDto;
import com.thanhnd.clinic_application.modules.chat.repository.ChatMessageRepository;
import com.thanhnd.clinic_application.modules.chat.service.ChatMessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {
	private final ChatMessageRepository chatMessageRepository;

	private final ChatMessageMapper chatMessageMapper;

	@Override
	public PageableResultDto<ChatMessageDto> findAllByChatBoxId(String chatBoxId, Pageable pageable) {
		Page<ChatMessage> chatMessagePage = chatMessageRepository.findAllByChatBoxId(chatBoxId, pageable);

		return PageableResultDto.parse(chatMessagePage.map(chatMessageMapper::toDtoExcludeChatBox));
	}
}
