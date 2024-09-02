package com.thanhnd.clinic_application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
public class ExampleController {
	@GetMapping("/public")
	public String getPublic() {
		return "This is a public API endpoint";
	}

	@GetMapping("/private")
	public String getPrivate() {
		return "This is a private API endpoint";
	}

	@GetMapping("/private-doctor")
	public String getPrivateRole() {
		return "This is a private API endpoint with role doctor";
	}
}
