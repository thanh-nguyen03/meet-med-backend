package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tbl_registered_shift")
public class RegisteredShift extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private Double shiftPrice;
	private Integer maxNumberOfPatients;
	private Boolean isApproved = false;

	@ManyToOne
	@JoinColumn(name = "shift_id", referencedColumnName = "id")
	private Shift shift;

	@ManyToOne
	@JoinColumn(name = "doctor_id", referencedColumnName = "id")
	private Doctor doctor;

	@ManyToOne
	@JoinColumn(name = "room_id", referencedColumnName = "id")
	private Room room;

	@OneToMany(mappedBy = "registeredShift")
	private List<RegisteredShiftTimeSlot> timeSlots;
}
