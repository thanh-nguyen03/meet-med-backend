package com.thanhnd.clinic_application.modules.departments.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddDoctorsDto {
	private List<String> doctorIds;
}
