package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.ChatMessage;
import com.thanhnd.clinic_application.modules.chat.dto.ChatMessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper extends IBaseMapper<ChatMessage, ChatMessageDto> {
	@Mapping(target = "chatBox", ignore = true)
	ChatMessageDto toDtoExcludeChatBox(ChatMessage entity);
}
