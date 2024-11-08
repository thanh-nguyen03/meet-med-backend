package com.thanhnd.clinic_application.modules.identity_providers.provider.mock.strategy;

import com.thanhnd.clinic_application.modules.identity_providers.interfaces.IdentityProviderStrategy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MockStrategy extends IdentityProviderStrategy {
	@Override
	public Map<String, Object> createUser(String email, String password) {
		// Mock implementation
		Map<String, Object> response = new HashMap<>();
		response.put("email", email);
		response.put("message", "User created in mock system");
		return response;
	}

	@Override
	public void deleteUser(String id) {
		// Mock implementation
		System.out.println("User deleted in mock system");
	}

	@Override
	public Map<String, Object> updateUser(String identityProviderUserId, Map<String, Object> userDto) {
		return Map.of();
	}

	@Override
	public Map<String, Object> getUserByEmail(String email) {
		// Mock implementation
		Map<String, Object> response = new HashMap<>();
		response.put("email", email);
		response.put("message", "User found in mock system");
		return response;
	}

	@Override
	public String resetUserPassword(Map<String, Object> user) {
		return "User password changed in mock system";
	}

	@Override
	public String assignRole(Map<String, Object> user, String role) {
		return null;
	}

	@Override
	public String getUserIdKey() {
		return "user_id";
	}
}
