package com.thanhnd.clinic_application.constants;

public class ValidationMessage {
	// ----------- User -----------
	public static final String USER_REQUIRED = "User is required";
	// Email
	public static final String EMAIL_INVALID = "Email address is invalid";
	public static final String EMAIL_REQUIRED = "Email address is required";

	// Password
	public static final String PASSWORD_REQUIRED = "Password is required";
	public static final String PASSWORD_LENGTH = "Password length must be at least 6 characters";

	// Full name
	public static final String FULL_NAME_LENGTH = "Full name length must be between 3 and 50 characters";
	public static final String FULL_NAME_REQUIRED = "Full name is required";

	// ----------- Doctor -----------
	public static final String DOCTOR_REQUIRED = "Doctor is required";
	// Years of experience
	public static final String DOCTOR_YEARS_OF_EXPERIENCE_REQUIRED = "Doctor's years of experience is required";
	public static final String DOCTOR_YEARS_OF_EXPERIENCE_INVALID = "Doctor's years of experience must be a positive number";

	// Degree
	public static final String DOCTOR_DEGREE_REQUIRED = "Doctor's degree is required";

	// Number of patients
	public static final String DOCTOR_NUMBER_OF_PATIENTS_REQUIRED = "Doctor's number of patients is required";

	// Number of certificates
	public static final String DOCTOR_NUMBER_OF_CERTIFICATES_REQUIRED = "Doctor's number of certificates is required";

	// Description
	public static final String DOCTOR_DESCRIPTION_REQUIRED = "Doctor's description is required";
	public static final String DOCTOR_DESCRIPTION_LENGTH = "Doctor's description length must not exceed 2000 characters";

	// Department
	public static final String DEPARTMENT_REQUIRED = "Department are required";

	// ----------- Department -----------
	// Name
	public static final String DEPARTMENT_NAME_REQUIRED = "Department name is required";
	public static final String DEPARTMENT_NAME_LENGTH = "Department name length must be between 3 and 50 characters";

	// Description
	public static final String DEPARTMENT_DESCRIPTION_REQUIRED = "Department description is required";
	public static final String DEPARTMENT_DESCRIPTION_LENGTH = "Department description length must not exceed 2000 characters";

	// ----------- Room -----------
	// Name
	public static final String ROOM_NAME_REQUIRED = "Room name is required";
	public static final String ROOM_NAME_LENGTH = "Room name length must be between 3 and 50 characters";

	// Department
	public static final String ROOM_DEPARTMENT_REQUIRED = "Room department is required";

	// ----------- Shift -----------
	public static final String SHIFT_REQUIRED = "Shift is required";

	// ----------- Registered Shift -----------
	public static final String REGISTERED_SHIFT_REQUIRED = "Registered shift is required";
	// Room
	public static final String ROOM_IS_REQUIRED = "Room is required";

	// ----------- Appointment -----------
	public static final String BOOK_TIME_REQUIRED = "Book time is required";
	public static final String TIME_SLOT_REQUIRED = "Shift time slot is required";
}
