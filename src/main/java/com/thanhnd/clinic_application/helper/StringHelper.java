package com.thanhnd.clinic_application.helper;

public class StringHelper {
	public static String totTitleCase(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		String[] words = input.split("\\s+");
		StringBuilder titleCase = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				titleCase.append(word.substring(0, 1).toUpperCase());
				titleCase.append(word.substring(1).toLowerCase());
				titleCase.append(" ");
			}
		}
		return titleCase.toString().trim();
	}
}
