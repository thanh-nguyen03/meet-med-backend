package com.thanhnd.clinic_application.modules.identity_providers.interfaces;

import java.util.Map;

public interface IdentityProviderUserManagementStrategy {
	Map<String, Object> createUser(String email, String password);

	void deleteUser(String id);

	Map<String, Object> updateUser(String identityProviderUserId, Map<String, Object> userDto);

	Map<String, Object> getUserByEmail(String email);

	String resetUserPassword(Map<String, Object> user);

	String assignRole(Map<String, Object> user, String role);
}
