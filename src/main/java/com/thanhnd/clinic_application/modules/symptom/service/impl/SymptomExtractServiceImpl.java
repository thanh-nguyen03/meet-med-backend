package com.thanhnd.clinic_application.modules.symptom.service.impl;

import com.thanhnd.clinic_application.modules.symptom.service.SymptomExtractService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SymptomExtractServiceImpl implements SymptomExtractService {
	@Value("${com.thanhnd.symptom-extract-service-url}")
	private String SYMPTOM_EXTRACTOR_URL;

	private final RestTemplate restTemplate;

	@Override
	public List<String> extractSymptoms(String text) {
		String url = SYMPTOM_EXTRACTOR_URL + "/extract-symptoms";

		Map<String, Object> body = new HashMap<>();
		body.put("input", text);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				return (List<String>) response.getBody().get("symptoms");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return List.of();
	}
}
