package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_doctor_shift_price_by_experience")
public class DoctorShiftPriceByExperience extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	private Integer experience;
	private Double multiplier;
}
