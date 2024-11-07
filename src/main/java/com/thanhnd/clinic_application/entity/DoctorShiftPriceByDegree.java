package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_doctor_shift_price_by_degree")
public class DoctorShiftPriceByDegree extends BaseEntity {
	public static final String BASE_DEGREE = "Default";

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(unique = true)
	private String degree;
	private Double basePrice;
}
