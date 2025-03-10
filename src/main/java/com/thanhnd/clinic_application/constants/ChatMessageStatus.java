package com.thanhnd.clinic_application.constants;

import lombok.Getter;

@Getter
public enum ChatMessageStatus {
	SENT("SENT"),
	READ("READ");

	private final String value;

	ChatMessageStatus(String value) {
		this.value = value;
	}
}
