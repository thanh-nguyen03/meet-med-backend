package com.thanhnd.clinic_application.modules.amqp.service.impl;

import com.thanhnd.clinic_application.modules.amqp.dto.AmqpMessage;
import com.thanhnd.clinic_application.modules.amqp.service.AmqpService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmqpServiceImpl implements AmqpService {
	private final Queue notificationQueue;
	private final RabbitTemplate rabbitTemplate;

	@Override
	public void produceMessage(AmqpMessage message) {
		rabbitTemplate.convertAndSend(notificationQueue.getName(), message);
	}
}
