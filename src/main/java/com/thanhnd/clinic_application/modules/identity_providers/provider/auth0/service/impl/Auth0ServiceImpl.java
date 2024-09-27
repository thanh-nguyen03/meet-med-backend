package com.thanhnd.clinic_application.modules.identity_providers.provider.auth0.service.impl;

import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.modules.identity_providers.provider.auth0.service.Auth0Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class Auth0ServiceImpl implements Auth0Service {
	private final RestTemplate restTemplate;

	@Value("${auth0.domain}")
	private String AUTH0_DOMAIN;

	@Value("${auth0.client-id}")
	private String AUTH0_CLIENT_ID;

	@Value("${auth0.client-secret}")
	private String AUTH0_CLIENT_SECRET;

	@Value("${auth0.audience}")
	private String AUTH0_AUDIENCE;


	@Override
	public String getAccessToken() {
		String url = String.format("https://%s/oauth/token", AUTH0_DOMAIN);

		Map<String, String> body = new HashMap<>();
		body.put("client_id", AUTH0_CLIENT_ID);
		body.put("client_secret", AUTH0_CLIENT_SECRET);
		body.put("audience", AUTH0_AUDIENCE);
		body.put("grant_type", "client_credentials");

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");

		HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

			if (response.getStatusCode().is2xxSuccessful()) {
				return Objects.requireNonNull(response.getBody()).get("access_token").toString();
			} else {
				throw new Exception("Failed to obtain Auth0 access token. Status code: " + response.getStatusCode());
			}
		} catch (HttpClientErrorException e) {
			throw HttpException.badRequest(e.getResponseBodyAsString());
		} catch (RestClientException e) {
			throw HttpException.internalServerError("An error occurred while trying to connect to Auth0: " + e.getMessage());
		} catch (Exception e) {
			throw HttpException.internalServerError("An error occurred while trying to obtain Auth0 access token: " + e.getMessage());
		}
	}
}
