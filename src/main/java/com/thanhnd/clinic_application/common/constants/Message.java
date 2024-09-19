package com.thanhnd.clinic_application.common.constants;

public enum Message {
	// Common
	OK("OK"),
	INTERNAL_SERVER_ERROR("Internal server error"),
	PERMISSION_DENIED("You do not have permission to access this resource!"),

	// Users
	USER_ID_NOT_FOUND("User with id %s not found"),
	USER_EMAIL_NOT_FOUND("User with email %s not found"),
	USER_EMAIL_ALREADY_EXISTS("User with email %s already exists");

	private final String message;

	Message(String message) {
		this.message = message;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}
}
