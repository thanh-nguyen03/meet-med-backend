package com.thanhnd.clinic_application.constants;

public class ValidationMessage {
	// ----------- User -----------
	// Email
	public static final String EMAIL_INVALID = "Email address is invalid";
	public static final String EMAIL_REQUIRED = "Email address is required";

	// Full name
	public static final String FULL_NAME_LENGTH = "Full name length must be between 3 and 50 characters";
	public static final String FULL_NAME_REQUIRED = "Full name is required";

	// ----------- Doctor -----------
	// Years of experience
	public static final String DOCTOR_YEARS_OF_EXPERIENCE_REQUIRED = "Doctor's years of experience is required";
	public static final String DOCTOR_YEARS_OF_EXPERIENCE_INVALID = "Doctor's years of experience must be a positive number";

	// Degree
	public static final String DOCTOR_DEGREE_REQUIRED = "Doctor's degree is required";

	// Description
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
}
