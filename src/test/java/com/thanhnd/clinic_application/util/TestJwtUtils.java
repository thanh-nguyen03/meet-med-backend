package com.thanhnd.clinic_application.util;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for JWT token management in tests
 */
public class TestJwtUtils {

	// A dummy JWT token for testing
	private static final String TEST_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXItaWQiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o";

	/**
	 * Adds authorization header with test JWT token to request builder
	 *
	 * @param builder The request builder to add the header to
	 * @return The request builder with authorization header
	 */
	public static MockHttpServletRequestBuilder addJwtAuthorization(MockHttpServletRequestBuilder builder) {
		return builder.header(HttpHeaders.AUTHORIZATION, TEST_JWT_TOKEN);
	}

	/**
	 * Adds authorization header with test JWT token to TestRestTemplate
	 *
	 * @param restTemplate The TestRestTemplate to add the header to
	 * @return The TestRestTemplate with authorization header
	 */
	public static TestRestTemplate addJwtAuthorization(TestRestTemplate restTemplate) {
		restTemplate.getRestTemplate().getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add(HttpHeaders.AUTHORIZATION, TEST_JWT_TOKEN);
			return execution.execute(request, body);
		});
		return restTemplate;
	}

	/**
	 * Sets up JWT token in security context for tests
	 *
	 * @param userId      The user ID to set in the token
	 * @param permissions Optional list of permissions to include in the token
	 */
	public static void setupJwtTokenInSecurityContext(String userId, List<String> permissions) {
		Jwt jwt = createJwtToken(userId, permissions);
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (permissions != null) {
			authorities = permissions.stream()
				.map(SimpleGrantedAuthority::new)
				.toList();
		}
		JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

		// Mock the SecurityContext to set the authentication token
		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(token);
		SecurityContextHolder.setContext(securityContext);
	}

	/**
	 * Sets up JWT token in security context for tests without permissions
	 *
	 * @param userId The user ID to set in the token
	 */
	public static void setupJwtTokenInSecurityContext(String userId) {
		setupJwtTokenInSecurityContext(userId, null);
	}

	/**
	 * Creates a JWT token for testing
	 *
	 * @param userId      The user ID to set in the token
	 * @param permissions Optional list of permissions to include in the token
	 * @return The created JWT token
	 */
	public static Jwt createJwtToken(String userId, List<String> permissions) {
		Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> claims = new HashMap<>();
		if (permissions != null) {
			claims.put("permissions", permissions);
		}
		claims.put("user_info", Map.of("id", userId));
		claims.put("sub", "auth0|" + userId);

		return new Jwt(
			"test-token-" + userId,
			Instant.now(),
			Instant.now().plusSeconds(3600),
			headers,
			claims
		);
	}
}