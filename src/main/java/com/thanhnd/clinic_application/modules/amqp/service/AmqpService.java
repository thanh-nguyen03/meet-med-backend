package com.thanhnd.clinic_application.modules.amqp.service;

import com.thanhnd.clinic_application.modules.amqp.dto.AmqpMessage;

public interface AmqpService {
	void produceMessage(String exchange, String routingKey, AmqpMessage message);
}
