package com.thanhnd.clinic_application.modules.symptom.service;

import java.util.List;

public interface SymptomExtractService {
	List<String> extractSymptoms(String text);
}
