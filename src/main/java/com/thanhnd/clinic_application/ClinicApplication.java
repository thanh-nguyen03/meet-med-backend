package com.thanhnd.clinic_application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
public class ClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicApplication.class, args);
	}
}
