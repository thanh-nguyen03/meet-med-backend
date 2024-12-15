package com.thanhnd.clinic_application.mapper;

import com.thanhnd.clinic_application.entity.ChatBox;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatBoxMapper extends IBaseMapper<ChatBox, ChatBoxDto> {
}
