package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.ChatBox;
import com.thanhnd.clinic_application.entity.ChatBoxMember;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxMemberDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatBoxMapper extends IBaseMapper<ChatBox, ChatBoxDto> {
	@Mapping(target = "members", ignore = true)
	ChatBoxDto toDtoExcludeMembers(ChatBox entity);

	@Mapping(target = "chatBox", ignore = true)
	ChatBoxMemberDto toMemberDto(ChatBoxMember entity);
}
