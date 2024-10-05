package com.thanhnd.clinic_application.constants;

public enum Message {
	// Common
	OK("OK"),
	INTERNAL_SERVER_ERROR("Internal server error"),
	PERMISSION_DENIED("You do not have permission to access this resource!"),

	// Users
	USER_NOT_FOUND("User not found"),
	USER_EMAIL_NOT_FOUND("User with email %s not found"),
	USER_EMAIL_ALREADY_EXISTS("User with email %s already exists"),
	USER_PASSWORD_CHANGED_SUCCESSFULLY("User password changed successfully"),
	USER_ROLE_ASSIGNED_SUCCESSFULLY("User role assigned successfully"),

	// Doctor
	DOCTOR_NOT_FOUND("Doctor not found"),

	// Department
	DEPARTMENT_NOT_FOUND("Department not found"),
	DEPARTMENT_NAME_ALREADY_EXISTS("Department with name %s already exists");


	private final String message;

	Message(String message) {
		this.message = message;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}
}
