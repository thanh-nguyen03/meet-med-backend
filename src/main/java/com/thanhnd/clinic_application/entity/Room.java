package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
	name = "tbl_room",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"name", "department_id"})
	})
@Getter
@Setter
public class Room extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String name;

	@ManyToOne
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;

	@OneToMany(mappedBy = "room")
	private List<RegisteredShift> registeredShifts;
}
