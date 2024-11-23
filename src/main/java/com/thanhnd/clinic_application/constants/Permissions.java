package com.thanhnd.clinic_application.constants;

public class Permissions {
	public static class Users {
		public static final String READ = "read:users";
		public static final String WRITE = "write:users";
		public static final String DELETE = "delete:users";
	}

	public static class Departments {
		public static final String READ = "read:departments";
		public static final String WRITE = "write:departments";
	}

	public static class Doctors {
		public static final String READ = "read:doctors";
		public static final String WRITE = "write:doctors";
	}

	public static class Room {
		public static final String READ = "read:rooms";
		public static final String WRITE = "write:rooms";
	}

	public static class Shift {
		public static final String READ = "read:shifts";
		public static final String WRITE = "write:shifts";
	}

	public static class RegisteredShift {
		public static final String READ = "read:registered_shifts";
		public static final String WRITE = "write:registered_shifts";
		public static final String APPROVE = "approve:registed-shift";
	}

	public static class Appointment {
		public static final String READ = "read:appointments";
		public static final String WRITE = "write:appointments";
	}
}
