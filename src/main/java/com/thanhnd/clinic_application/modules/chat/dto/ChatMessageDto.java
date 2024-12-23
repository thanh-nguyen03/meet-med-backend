package com.thanhnd.clinic_application.modules.chat.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.constants.ChatMessageStatus;
import com.thanhnd.clinic_application.constants.ChatMessageType;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessageDto extends BaseDto {
	private String id;
	private String content;
	private ChatMessageType type = ChatMessageType.TEXT;
	private ChatMessageStatus status = ChatMessageStatus.SENT;

	private PatientDto patient;
	private DoctorDto doctor;
	private ChatBoxDto chatBox;
}
