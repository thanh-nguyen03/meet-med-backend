package com.thanhnd.clinic_application.constants;

import lombok.Getter;

@Getter
public enum ChatMessageType {
	TEXT("TEXT"),
	IMAGE("IMAGE"),
	VIDEO("VIDEO"),
	AUDIO("AUDIO"),
	FILE("FILE"),
	NOTIFICATION("NOTIFICATION");

	private final String value;

	ChatMessageType(String value) {
		this.value = value;
	}
}
