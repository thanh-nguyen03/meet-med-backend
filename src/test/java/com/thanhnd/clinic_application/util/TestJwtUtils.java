package com.thanhnd.clinic_application.util;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Utility class for JWT token management in tests
 */
public class TestJwtUtils {

    // A dummy JWT token for testing
    private static final String TEST_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXItaWQiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o";

    /**
     * Adds authorization header with test JWT token to request builder
     * @param builder The request builder to add the header to
     * @return The request builder with authorization header
     */
    public static MockHttpServletRequestBuilder addJwtAuthorization(MockHttpServletRequestBuilder builder) {
        return builder.header(HttpHeaders.AUTHORIZATION, TEST_JWT_TOKEN);
    }

    /**
     * Adds authorization header with test JWT token to TestRestTemplate
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
} 