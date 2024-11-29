package com.thanhnd.clinic_application.modules.appointments.service;

public interface AppointmentReminderService {
	void send24HourReminder();

	void send1HourReminder();
}
