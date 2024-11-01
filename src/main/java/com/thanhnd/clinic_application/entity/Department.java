package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "tbl_department")
public class Department extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private String description;

	@OneToMany(mappedBy = "department")
	private List<Doctor> doctors;

	@OneToOne
	@JoinColumn(name = "head_doctor_id", referencedColumnName = "id")
	private Doctor headDoctor;

	@OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
	private List<Room> rooms;
}
