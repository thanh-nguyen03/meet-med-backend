package com.thanhnd.clinic_application.configuration;

import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

/**
 * Test security configuration that disables security for tests
 * but provides necessary beans for JWT authentication
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfiguration {

	/**
	 * Configures security to permit all requests in test environment
	 */
	@Bean
	@Primary
	public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

	/**
	 * Provides a mock JwtAuthenticationManager for tests
	 */
	@Bean
	@Primary
	public JwtAuthenticationManager jwtAuthenticationManager() {
		return mock(JwtAuthenticationManager.class);
	}
}
