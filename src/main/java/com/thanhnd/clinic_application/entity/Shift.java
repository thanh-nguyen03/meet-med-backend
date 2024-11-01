package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tbl_shift")
public class Shift {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private Instant startTime;
	private Instant endTime;

	@OneToMany(mappedBy = "shift")
	private List<RegisteredShift> registeredShifts;
}
