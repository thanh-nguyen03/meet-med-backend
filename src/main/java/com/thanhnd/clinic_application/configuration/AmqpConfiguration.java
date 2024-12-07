package com.thanhnd.clinic_application.configuration;

import com.thanhnd.clinic_application.constants.MessageQueueConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfiguration {
	@Bean
	public Queue notificationQueue() {
		return new Queue(MessageQueueConstants.QueueName.NOTIFICATION_QUEUE, true);
	}

	@Bean
	public Queue chatQueue() {
		return new Queue(MessageQueueConstants.QueueName.CHAT_QUEUE, true);
	}

	@Bean
	public DirectExchange notificationExchange() {
		return new DirectExchange(MessageQueueConstants.ExchangeName.NOTIFICATION_EXCHANGE);
	}

	@Bean
	public DirectExchange chatExchange() {
		return new DirectExchange(MessageQueueConstants.ExchangeName.CHAT_EXCHANGE);
	}

	@Bean
	public Binding notificationBinding() {
		return BindingBuilder
			.bind(notificationQueue())
			.to(notificationExchange())
			.with(MessageQueueConstants.RoutingKey.NOTIFICATION_ROUTING_KEY);
	}

	@Bean
	Binding chatBinding() {
		return BindingBuilder
			.bind(chatQueue())
			.to(chatExchange())
			.with(MessageQueueConstants.RoutingKey.CHAT_ROUTING);
	}

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter());
		return rabbitTemplate;
	}
}
