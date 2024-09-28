package com.thanhnd.clinic_application.modules.identity_providers.provider.auth0.strategy;

import com.thanhnd.clinic_application.common.exception.Auth0Exception;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.identity_providers.provider.auth0.service.Auth0Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Auth0Strategy extends IdentityProviderStrategy {
	private final RestTemplate restTemplate;
	private final Auth0Service auth0Service;

	@Value("${auth0.domain}")
	private String AUTH0_DOMAIN;

	@Override
	public String getUserIdKey() {
		return "user_id";
	}

	@Override
	public Map<String, Object> createUser(String email, String password) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users";

		Map<String, Object> body = new HashMap<>();
		body.put("email", email);
		body.put("password", password);
		body.put("connection", "Username-Password-Authentication");

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", String.format("Bearer %s", accessToken));
		headers.add("Content-Type", "application/json");

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				url, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class
			);

			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			}
		} catch (RestClientException e) {
			Auth0Exception auth0Exception = Auth0Exception.fromJson(e.getMessage());
			if (auth0Exception.getStatusCode().is4xxClientError()) {
				throw HttpException.badRequest(auth0Exception.getMessage());
			} else {
				throw HttpException.internalServerError(auth0Exception.getMessage());
			}
		}

		return null;
	}

	@Override
	public void deleteUser(String id) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users/" + id;

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-Type", "application/json");

		HttpEntity<Void> request = new HttpEntity<>(headers);

		restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
	}

	@Override
	public Map<String, Object> getUserByEmail(String email) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users-by-email?email=" + email;

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-Type", "application/json");

		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>[]> response = restTemplate.exchange(
			url,
			HttpMethod.GET,
			request,
			(Class<Map<String, Object>[]>) (Class<?>) Map[].class
		);

		if (response.getStatusCode().is2xxSuccessful()) {
			Map<String, Object>[] users = response.getBody();
			if (users != null && users.length > 0) {
				return users[0];
			} else {
				throw HttpException.notFound(Message.USER_EMAIL_NOT_FOUND.getMessage(email));
			}
		} else {
			throw HttpException.internalServerError(Message.INTERNAL_SERVER_ERROR.getMessage());
		}
	}
}
