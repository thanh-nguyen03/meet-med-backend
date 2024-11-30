package com.thanhnd.clinic_application.constants;

public enum NotificationMessage {
	APPOINTMENT_BOOKED_TITLE("Appointment booked successfully"),
	APPOINTMENT_BOOKED_MESSAGE("Your appointment has been booked successfully. Please check the detail information."),

	APPOINTMENT_REMINDER_TITLE("Appointment Reminder"),
	APPOINTMENT_REMINDER_24_HOUR_MESSAGE("You have an appointment with Doctor %s at %s. Please check the detail information."),
	APPOINTMENT_REMINDER_1_HOUR_MESSAGE("You have an appointment with Doctor %s in 1 hour. Please check the detail information."),

	WORKING_SHIFT_REMINDER_TITLE("Working Shift Reminder"),
	WORKING_SHIFT_REMINDER_TOMORROW_MESSAGE("You have a working shift tomorrow. Please check the detail information."),
	;

	private final String message;

	NotificationMessage(String message) {
		this.message = message;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}
}
