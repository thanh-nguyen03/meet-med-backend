package com.thanhnd.clinic_application.modules.chat.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.modules.users.dto.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatBoxMemberDto extends BaseDto {
	private String id;
	private UserDto user;
	private ChatBoxDto chatBox;
}
