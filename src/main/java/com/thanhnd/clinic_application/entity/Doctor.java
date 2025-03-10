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
@Table(name = "tbl_doctor")
public class Doctor extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private Integer yearsOfExperience;
	private String degree;
	private Integer numberOfPatients;
	private Integer numberOfCertificates;

	@Column(length = 2000)
	private String description;

	@OneToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "department_id", referencedColumnName = "id")
	private Department department;

	@OneToOne(mappedBy = "headDoctor")
	private Department headOfDepartment;

	@OneToMany(mappedBy = "doctor")
	private List<RegisteredShift> registeredShifts;
}
