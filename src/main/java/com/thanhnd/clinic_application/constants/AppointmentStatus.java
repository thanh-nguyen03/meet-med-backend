package com.thanhnd.clinic_application.constants;

import lombok.Getter;

@Getter
public enum AppointmentStatus {
	UPCOMING("UPCOMING"),
	COMPLETED("COMPLETED"),
	CANCELLED("CANCELLED");

	private final String value;

	AppointmentStatus(String value) {
		this.value = value;
	}
}
