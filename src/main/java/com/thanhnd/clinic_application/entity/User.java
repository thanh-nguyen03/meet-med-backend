package com.thanhnd.clinic_application.entity;

import com.thanhnd.clinic_application.common.constants.Role;
import com.thanhnd.clinic_application.common.constants.UserGender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "tbl_user")
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false, unique = true)
	private String email;

	private String fullName;
	private Integer age;
	private String phone;

	@Column(nullable = false)
	private UserGender gender = UserGender.Male;

	@Column(nullable = false)
	private Role role = Role.User;

	@OneToOne
	@JoinColumn(name = "address_id", referencedColumnName = "id")
	private Address address;

	@OneToOne
	@JoinColumn(name = "doctor_information_id", referencedColumnName = "id")
	private DoctorInformation doctorInformation;
}
