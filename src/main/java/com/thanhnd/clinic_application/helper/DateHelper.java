package com.thanhnd.clinic_application.helper;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;

public class DateHelper {
	public static ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

	public static Instant getFirstDayOfMonth(int month, int year) {
		return Instant.parse(year + "-" + month + "-01T00:00:00Z");
	}

	public static Instant getLastDayOfMonth(int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);

		int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		return Instant.parse(year + "-" + month + "-" + lastDay + "T23:59:59Z");
	}
}
