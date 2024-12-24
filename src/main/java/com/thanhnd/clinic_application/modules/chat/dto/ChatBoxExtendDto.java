package com.thanhnd.clinic_application.modules.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatBoxExtendDto extends ChatBoxDto {
	private ChatMessageDto lastMessage;

	public ChatBoxExtendDto(ChatBoxDto chatBoxDto, ChatMessageDto lastMessage) {
		setId(chatBoxDto.getId());
		setPatient(chatBoxDto.getPatient());
		setDoctor(chatBoxDto.getDoctor());
		setCreatedAt(chatBoxDto.getCreatedAt());
		setUpdatedAt(chatBoxDto.getUpdatedAt());
		this.lastMessage = lastMessage;
	}
}
