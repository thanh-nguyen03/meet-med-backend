package com.thanhnd.clinic_application.modules.appointments.specification;

import com.thanhnd.clinic_application.constants.AppointmentStatus;
import com.thanhnd.clinic_application.entity.Appointment;
import org.springframework.data.jpa.domain.Specification;

public class AppointmentSpecification {
	public static Specification<Appointment> hasStatus(AppointmentStatus status) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
	}

	public static Specification<Appointment> inRegisteredShift(String registeredShiftId) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
			root.get("registeredShiftTimeSlot").get("registeredShift").get("id"),
			registeredShiftId
		);
	}

	public static Specification<Appointment> ofDoctor(String doctorId) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
			root.get("registeredShiftTimeSlot").get("registeredShift").get("doctor").get("id"),
			doctorId
		);
	}

	public static Specification<Appointment> ofPatientUserId(String userId) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
			root.get("patient").get("user").get("id"),
			userId
		);
	}
}
