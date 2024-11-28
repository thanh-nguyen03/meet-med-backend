package com.thanhnd.clinic_application.constants;

public enum Message {
	// Common
	OK("OK"),
	INTERNAL_SERVER_ERROR("Internal server error"),
	PERMISSION_DENIED("You do not have permission to access this resource!"),
	UNAUTHORIZED("Unauthorized"),
	INVALID_REQUEST("Invalid request"),
	INVALID_SEARCH("Invalid search criteria: %s"),

	// Users
	USER_NOT_FOUND("User not found"),
	USER_EMAIL_NOT_FOUND("User with email %s not found"),
	USER_EMAIL_ALREADY_EXISTS("User with email %s already exists"),
	USER_PASSWORD_CHANGED_SUCCESSFULLY("User password changed successfully"),
	USER_ROLE_ASSIGNED_SUCCESSFULLY("User role assigned successfully"),

	// Doctor
	DOCTOR_NOT_FOUND("Doctor not found"),
	DOCTOR_NOT_IN_DEPARTMENT("Doctor %s not in department"),

	// Department
	DEPARTMENT_NOT_FOUND("Department not found"),
	DEPARTMENT_NAME_ALREADY_EXISTS("Department with name %s already exists"),

	// Room
	ROOM_NOT_FOUND("Room not found"),
	ROOM_NAME_ALREADY_EXISTS("Room with name %s already exists in department %s"),

	// Shifts
	SHIFT_FOR_MONTH_ALREADY_CREATED("Shifts for %d/%d already created"),

	SHIFT_NOT_FOUND("Shift not found"),
	SHIFT_FULL("Shift is full"),
	DOCTOR_ALREADY_REGISTERED_FOR_SHIFT("Doctor already registered for shift"),

	// Registered Shift
	REGISTERED_SHIFT_NOT_FOUND("Registered shift not found"),
	CANNOT_REGISTER_MULTIPLE_DOCTORS("Cannot register for multiple doctors"),
	DUPLICATE_SHIFTS_IN_REQUEST("Duplicate shifts in request"),
	SHIFT_ALREADY_APPROVED("Shift already approved"),
	ROOM_ALREADY_ASSIGNED("Room already assigned to another shift"),
	ROOM_NOT_IN_DEPARTMENT("Room not in the same department as the doctor"),

	// Patient
	PATIENT_NOT_FOUND("Patient profile not found"),
	PATIENT_HAS_NOT_BEEN_CREATED("Patient profile has not been created"),
	PATIENT_ALREADY_EXISTS("Patient profile already exists"),

	// Time slot
	TIME_SLOT_NOT_FOUND("Time slot not found"),
	TIME_SLOT_FULL("Time slot is full"),
	TIME_SLOT_ALREADY_BOOKED("You have already booked this time slot"),

	// Appointment
	APPOINTMENT_NOT_FOUND("Appointment not found"),

	// Notification
	NOTIFICATION_NOT_FOUND("Notification not found"),
	;

	private final String message;

	Message(String message) {
		this.message = message;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}
}
