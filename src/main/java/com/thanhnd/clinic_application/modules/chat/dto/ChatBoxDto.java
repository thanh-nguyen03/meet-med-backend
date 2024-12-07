package com.thanhnd.clinic_application.modules.chat.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatBoxDto extends BaseDto {
	private String id;
	private String socketRoomName;
	private List<ChatBoxMemberDto> members;
}
