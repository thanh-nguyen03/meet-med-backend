package com.thanhnd.clinic_application.constants;

import lombok.Getter;

@Getter
public enum NotificationType {
	// For patient
	BOOK_APPOINTMENT_SUCCESS("BOOK_APPOINTMENT_SUCCESS"),
	CANCEL_APPOINTMENT_SUCCESS("CANCEL_APPOINTMENT_SUCCESS"),
	APPOINTMENT_REMINDER("APPOINTMENT_REMINDER"),

	// For doctor
	WORKING_SHIFT_REMINDER("WORKING_SHIFT_REMINDER");

	private final String value;

	NotificationType(String value) {
		this.value = value;
	}
}
