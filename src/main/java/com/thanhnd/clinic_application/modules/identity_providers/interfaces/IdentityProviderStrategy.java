package com.thanhnd.clinic_application.modules.identity_providers.interfaces;

public abstract class IdentityProviderStrategy implements IdentityProviderUserManagementStrategy {
	abstract public String getUserIdKey();
}
