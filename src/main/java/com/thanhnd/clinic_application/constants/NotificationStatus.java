package com.thanhnd.clinic_application.constants;

import lombok.Getter;

@Getter
public enum NotificationStatus {
	OPEN("OPEN"),
	READ("READ");

	private final String value;

	NotificationStatus(String value) {
		this.value = value;
	}
}
