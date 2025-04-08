package com.thanhnd.clinic_application.util;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Helper class for creating MockHttpServletRequestBuilder instances without JWT token
 * This is used to replace the TestJwtUtils.addJwtAuthorization method
 */
public class MockMvcRequestBuilderHelper {

	/**
	 * Creates a GET request builder
	 *
	 * @param url The URL to send the request to
	 * @return The request builder
	 */
	public static MockHttpServletRequestBuilder get(String url) {
		return MockMvcRequestBuilders.get(url)
			.contentType(MediaType.APPLICATION_JSON);
	}

	/**
	 * Creates a POST request builder
	 *
	 * @param url The URL to send the request to
	 * @return The request builder
	 */
	public static MockHttpServletRequestBuilder post(String url) {
		return MockMvcRequestBuilders.post(url)
			.contentType(MediaType.APPLICATION_JSON);
	}

	/**
	 * Creates a PUT request builder
	 *
	 * @param url The URL to send the request to
	 * @return The request builder
	 */
	public static MockHttpServletRequestBuilder put(String url) {
		return MockMvcRequestBuilders.put(url)
			.contentType(MediaType.APPLICATION_JSON);
	}

	/**
	 * Creates a DELETE request builder
	 *
	 * @param url The URL to send the request to
	 * @return The request builder
	 */
	public static MockHttpServletRequestBuilder delete(String url) {
		return MockMvcRequestBuilders.delete(url)
			.contentType(MediaType.APPLICATION_JSON);
	}
}
