package com.thanhnd.clinic_application.modules.amqp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class AmqpMessage {
	private Instant timestamp;
	private Object content;
}
