package com.thanhnd.clinic_application.modules.chat.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.modules.doctors.dto.DoctorDto;
import com.thanhnd.clinic_application.modules.patients.dto.PatientDto;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ChatBoxDto extends BaseDto {
	private String id;
	private PatientDto patient;
	private DoctorDto doctor;
}
