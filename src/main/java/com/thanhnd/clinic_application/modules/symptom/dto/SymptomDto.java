package com.thanhnd.clinic_application.modules.symptom.dto;

import com.thanhnd.clinic_application.common.dto.BaseDto;
import com.thanhnd.clinic_application.modules.departments.dto.DepartmentDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymptomDto extends BaseDto {
	private String id;
	private String name;

	private List<DepartmentDto> departments;
}
