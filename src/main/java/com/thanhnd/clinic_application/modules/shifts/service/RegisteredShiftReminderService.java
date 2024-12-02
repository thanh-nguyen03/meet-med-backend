package com.thanhnd.clinic_application.modules.shifts.service;

public interface RegisteredShiftReminderService {
	void sendTomorrowShiftReminder();

	void sendMockReminder(String registeredShiftId);
}
