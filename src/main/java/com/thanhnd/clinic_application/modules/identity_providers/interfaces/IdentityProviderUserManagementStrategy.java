package com.thanhnd.clinic_application.modules.identity_providers.interfaces;

import java.util.Map;

public interface IdentityProviderUserManagementStrategy {
	Map<String, Object> createUser(String email, String password);

	void deleteUser(String id);

	Map<String, Object> getUserByEmail(String email);
}
