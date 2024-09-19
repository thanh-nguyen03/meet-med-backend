package com.thanhnd.clinic_application.common.service;

import java.util.List;

public interface JwtAuthenticationManager {
	Boolean isAuthenticated();

	<T> T getClaim(String key);

	List<String> getUserPermissions();
}
