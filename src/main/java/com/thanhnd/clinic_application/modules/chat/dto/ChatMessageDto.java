package com.thanhnd.clinic_application.modules.chat.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.ChatMessageStatus;
import com.thanhnd.clinic_application.constants.ChatMessageType;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessageDto extends BaseDto {
	private String id;
	private String content;
	private ChatMessageType type = ChatMessageType.TEXT;
	private ChatMessageStatus status = ChatMessageStatus.SENT;

	private UserDto sender;
	private ChatBoxDto chatBox;
}
