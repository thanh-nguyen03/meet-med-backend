package com.thanhnd.clinic_application.modules.symptom.service;


import com.thanhnd.clinic_application.modules.symptom.dto.SymptomDto;

import java.util.List;

public interface SymptomService {
	List<SymptomDto> findAll();

}
