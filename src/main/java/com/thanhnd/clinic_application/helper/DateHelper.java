package com.thanhnd.clinic_application.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

public class DateHelper {
	public static ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

	public static Instant getFirstDayOfMonth(int month, int year) {
		return Instant.parse(year + "-" + month + "-01T00:00:00Z");
	}

	public static Instant getLastDayOfMonth(int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month - 1); // Month is zero-based
		calendar.set(Calendar.YEAR, year);

		int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		return Instant.parse(year + "-" + String.format("%02d", month) + "-" + String.format("%02d", lastDay) + "T23:59:59Z");
	}

	public static Instant getStartOfDay(LocalDate date) {
		return date.atStartOfDay(ZONE_ID).toInstant();
	}

	public static Instant getEndOfDay(LocalDate date) {
		return date.atTime(23, 59, 59).atZone(ZONE_ID).toInstant();
	}

	public static LocalDate getStartOfWeek(LocalDate date) {
		return date.minusDays(date.getDayOfWeek().getValue() - 1);
	}

	public static LocalDate getEndOfWeek(LocalDate date) {
		return date.plusDays(7 - date.getDayOfWeek().getValue());
	}
}
