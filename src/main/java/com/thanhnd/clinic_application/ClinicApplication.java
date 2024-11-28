package com.thanhnd.clinic_application;

import com.google.gson.JsonObject;
import com.thanhnd.clinic_application.constants.NotificationType;
import com.thanhnd.clinic_application.helper.NotificationHelper;
import com.thanhnd.clinic_application.modules.amqp.dto.AmqpMessage;
import com.thanhnd.clinic_application.modules.amqp.service.AmqpService;
import com.thanhnd.clinic_application.modules.notifications.dto.AmqpNotificationMessageDto;
import com.thanhnd.clinic_application.modules.notifications.dto.NotificationDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.time.Instant;

@SpringBootApplication
@EnableMethodSecurity
public class ClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(AmqpService amqpService) {
		return args -> {
			NotificationDto notificationDto = new NotificationDto();
			notificationDto.setTitle("Hello");
			notificationDto.setContent("World");
			notificationDto.setReceiverId("123");
			notificationDto.setType(NotificationType.APPOINTMENT_REMINDER);
			JsonObject data = new JsonObject();
			data.addProperty("key", "value");
			notificationDto.setObjectData(data.toString());

			AmqpNotificationMessageDto notificationMessageDto = new AmqpNotificationMessageDto();
			notificationMessageDto.setNotification(notificationDto);
			notificationMessageDto.setRoomName(NotificationHelper.getNotificationRoomNameByUserId(notificationDto.getReceiverId()));
			amqpService.produceMessage(
				AmqpMessage.builder()
					.timestamp(Instant.now())
					.content(notificationMessageDto)
					.build()
			);
		};
	}
}
