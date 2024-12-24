package com.thanhnd.clinic_application.modules.chat.service.impl;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.ChatMessageStatus;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.ChatBox;
import com.thanhnd.clinic_application.entity.ChatMessage;
import com.thanhnd.clinic_application.mapper.ChatMessageMapper;
import com.thanhnd.clinic_application.modules.chat.dto.ChatMessageDto;
import com.thanhnd.clinic_application.modules.chat.repository.ChatBoxRepository;
import com.thanhnd.clinic_application.modules.chat.repository.ChatMessageRepository;
import com.thanhnd.clinic_application.modules.chat.service.ChatMessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {
	private final ChatBoxRepository chatBoxRepository;
	private final ChatMessageRepository chatMessageRepository;

	private final ChatMessageMapper chatMessageMapper;

	private final JwtAuthenticationManager jwtAuthenticationManager;

	@Override
	public PageableResultDto<ChatMessageDto> findAllByChatBoxId(String chatBoxId, Pageable pageable) {
		String userId = jwtAuthenticationManager.getUserId();
		ChatBox chatBox = chatBoxRepository.findById(chatBoxId)
			.orElseThrow(() -> HttpException.notFound(Message.CHAT_BOX_NOT_FOUND.getMessage()));

		String patientUserId = chatBox.getPatient().getUser().getId();
		String doctorUserId = chatBox.getDoctor().getUser().getId();

		if (!userId.equals(patientUserId) && !userId.equals(doctorUserId)) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		Page<ChatMessage> chatMessagePage = chatMessageRepository.findAllByChatBoxId(chatBoxId, pageable);

		return PageableResultDto.parse(chatMessagePage.map(chatMessageMapper::toDtoExcludeChatBox));
	}

	@Override
	public void readAllMessages(String chatBoxId) {
		String userId = jwtAuthenticationManager.getUserId();
		ChatBox chatBox = chatBoxRepository.findById(chatBoxId)
			.orElseThrow(() -> HttpException.notFound(Message.CHAT_BOX_NOT_FOUND.getMessage()));

		String patientUserId = chatBox.getPatient().getUser().getId();
		String doctorUserId = chatBox.getDoctor().getUser().getId();

		if (!userId.equals(patientUserId) && !userId.equals(doctorUserId)) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
		Page<ChatMessage> unreadMessages = chatMessageRepository.findAllByChatBoxIdAndStatusRead(chatBoxId, pageRequest);

		List<ChatMessage> unreadMessagesList = unreadMessages.getContent();
		unreadMessagesList.forEach(chatMessage -> chatMessage.setStatus(ChatMessageStatus.READ));

		chatMessageRepository.saveAll(unreadMessagesList);
	}
}
