package com.thanhnd.clinic_application.modules.symptom.service.impl;

import com.thanhnd.clinic_application.mapper.SymptomMapper;
import com.thanhnd.clinic_application.modules.symptom.dto.SymptomDto;
import com.thanhnd.clinic_application.modules.symptom.repository.SymptomRepository;
import com.thanhnd.clinic_application.modules.symptom.service.SymptomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SymptomServiceImpl implements SymptomService {
	private final SymptomRepository symptomRepository;

	private final SymptomMapper symptomMapper;

	@Override
	public List<SymptomDto> findAll() {
		return symptomRepository.findAll().stream().map(symptomMapper::toDto).toList();
	}
}
