package com.thanhnd.clinic_application.constants;

public class MessageQueueConstants {
	public static class QueueName {
		public static final String NOTIFICATION_QUEUE = "notificationQueue";
		public static final String CHAT_QUEUE = "chatQueue";
	}

	public static class ExchangeName {
		public static final String NOTIFICATION_EXCHANGE = "notificationExchange";
		public static final String CHAT_EXCHANGE = "chatExchange";
	}

	public static class RoutingKey {
		public static final String NOTIFICATION_ROUTING_KEY = "notification";
		public static final String CHAT_ROUTING = "chat";
	}
}
