package com.thanhnd.clinic_application.entity;

import com.thanhnd.clinic_application.constants.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_appointment")
public class Appointment extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private String symptoms;
	private AppointmentStatus status = AppointmentStatus.UPCOMING;

	@ManyToOne
	@JoinColumn(name = "registered_shift_time_slot_id", referencedColumnName = "id")
	private RegisteredShiftTimeSlot registeredShiftTimeSlot;

	@ManyToOne
	@JoinColumn(name = "patient_id", referencedColumnName = "id")
	private Patient patient;
}
