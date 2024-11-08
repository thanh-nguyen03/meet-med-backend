package com.thanhnd.clinic_application.modules.identity_providers.provider.auth0.strategy;

import com.thanhnd.clinic_application.common.exception.Auth0Exception;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import com.thanhnd.clinic_application.modules.identity_providers.provider.auth0.service.Auth0Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Auth0Strategy extends IdentityProviderStrategy {
	private final RestTemplate restTemplate;
	private final Auth0Service auth0Service;

	@Value("${auth0.domain}")
	private String AUTH0_DOMAIN;

	@Value("${auth0.default-user-password}")
	private String DEFAULT_USER_PASSWORD;

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
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

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
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		HttpEntity<Void> request = new HttpEntity<>(headers);

		restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
	}

	@Override
	public Map<String, Object> updateUser(String identityProviderUserId, Map<String, Object> userDto) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users/" + identityProviderUserId;

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		Map<String, Object> body = new HashMap<>();
		body.put("name", userDto.get("fullName"));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				url, HttpMethod.PATCH, request, (Class<Map<String, Object>>) (Class<?>) Map.class
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

		return Map.of();
	}

	@Override
	public Map<String, Object> getUserByEmail(String email) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users-by-email?email=" + email;

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

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

	@Override
	public String resetUserPassword(Map<String, Object> user) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users/" + user.get(getUserIdKey());

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		Map<String, Object> body = new HashMap<>();
		body.put("password", DEFAULT_USER_PASSWORD);
		body.put("connection", ((Map<?, ?>) ((ArrayList<?>) user.get("identities")).get(0)).get("connection"));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
			url,
			HttpMethod.PATCH,
			request,
			(Class<Map<String, Object>>) (Class<?>) Map.class
		);

		if (response.getStatusCode().is2xxSuccessful()) {
			return Message.USER_PASSWORD_CHANGED_SUCCESSFULLY.getMessage();
		} else if (response.getStatusCode().is4xxClientError()) {
			throw HttpException.badRequest(Message.USER_NOT_FOUND.getMessage());
		} else {
			throw HttpException.internalServerError(Message.INTERNAL_SERVER_ERROR.getMessage());
		}
	}

	@Override
	public String assignRole(Map<String, Object> user, String role) {
		String accessToken = auth0Service.getAccessToken();
		String url = "https://" + AUTH0_DOMAIN + "/api/v2/users/" + user.get(getUserIdKey()) + "/roles";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

		Map<String, Object> body = new HashMap<>();
		body.put("roles", List.of(role));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				url, HttpMethod.POST, request, (Class<Map<String, Object>>) (Class<?>) Map.class
			);

			if (response.getStatusCode().is2xxSuccessful()) {
				return Message.USER_ROLE_ASSIGNED_SUCCESSFULLY.getMessage();
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
}
