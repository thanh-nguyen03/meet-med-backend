package com.thanhnd.clinic_application.common.service.impl;

import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.JwtConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationManagerImpl implements JwtAuthenticationManager {
	private Map<String, Object> claims;
	private Boolean authenticated;

	@Override
	public Boolean isAuthenticated() {
		initClaims();
		return authenticated;
	}

	@Override
	public Map<String, Object> getClaims() {
		initClaims();
		return claims;
	}

	@Override
	public String getUserId() {
		initClaims();
		Map<String, Object> user = getClaim(JwtConstants.JWT_USER_INFO_CLAIM);
		Object id = Objects.requireNonNullElse(user, Map.of()).get("id");

		return Optional.ofNullable(id).map(Object::toString).orElse(null);
	}

	@Override
	public String getIdentityProviderUserId() {
		initClaims();
		String sub = getClaim(JwtConstants.JWT_SUB_CLAIM);

		return sub.split("\\|")[1];
	}

	@Override
	public <T> T getClaim(String key) {
		initClaims();
		try {
			return (T) claims.get(key);
		} catch (ClassCastException e) {
			log.error("Error casting claim value", e);
		}
		return null;
	}

	@Override
	public List<String> getUserPermissions() {
		initClaims();
		return Objects.requireNonNullElse(getClaim(JwtConstants.JWT_PERMISSIONS_CLAIM), List.of());
	}

	private void initClaims() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof JwtAuthenticationToken)) {
			authenticated = false;
			claims = Map.of();
			return;
		}
		Jwt jwt = (Jwt) authentication.getPrincipal();
		authenticated = true;
		claims = jwt.getClaims();
	}
}
