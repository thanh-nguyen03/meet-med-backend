package com.thanhnd.clinic_application.constants;

public enum NotificationMessage {
	APPOINTMENT_BOOKED_TITLE("Appointment booked successfully"),
	APPOINTMENT_BOOKED_MESSAGE("Your appointment has been booked successfully. Please check the detail information."),
	;

	private final String message;

	NotificationMessage(String message) {
		this.message = message;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}
}
