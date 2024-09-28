package com.thanhnd.clinic_application.common.exception;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.nimbusds.jose.shaded.gson.stream.JsonReader;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.StringReader;

@Getter
public class Auth0Exception {
	private final HttpStatus statusCode;
	private final String error;
	private final String message;

	public Auth0Exception(Integer statusCode, String error, String message) {
		this.statusCode = HttpStatus.valueOf(statusCode);
		this.error = error;
		this.message = message;
	}

	public static Auth0Exception fromJson(String message) {
		String json = message.substring(message.indexOf("{"), message.lastIndexOf("}") + 1);
		JsonReader reader = new JsonReader(new StringReader(json));
		reader.setLenient(true);
		JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

		return new Auth0Exception(
			jsonObject.get("statusCode").getAsInt(),
			jsonObject.get("error").getAsString(),
			jsonObject.get("message").getAsString()
		);
	}
}
