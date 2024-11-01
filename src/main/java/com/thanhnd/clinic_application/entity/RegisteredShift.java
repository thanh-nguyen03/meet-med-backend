package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_registered_shift")
public class RegisteredShift {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private Integer shiftPrice;
	private Integer maxNumberOfPatients;

	@ManyToOne
	@JoinColumn(name = "shift_id", referencedColumnName = "id")
	private Shift shift;

	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctor doctor;

	@ManyToOne
	@JoinColumn(name = "room_id", referencedColumnName = "id")
	private Room room;
}
