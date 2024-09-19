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
@Table(name = "tbl_doctor_information")
public class DoctorInformation extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private Integer yearOfExperience;
	private String degree;
	private String description;
	private Boolean isActive;

	@OneToOne(mappedBy = "doctorInformation")
	private User user;

	@ManyToMany
	@JoinTable(
		name = "tbl_doctor_categories",
		joinColumns = @JoinColumn(name = "doctor_information_id"),
		inverseJoinColumns = @JoinColumn(name = "categories_id")
	)
	private List<Categories> categories;
}
