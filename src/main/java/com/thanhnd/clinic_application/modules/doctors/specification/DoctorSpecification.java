package com.thanhnd.clinic_application.modules.doctors.specification;

import com.thanhnd.clinic_application.entity.Doctor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class DoctorSpecification {
	public static Specification<Doctor> filterByNameAndDepartment(String searchName, String searchDepartment) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (searchName != null && !searchName.isEmpty()) {
				predicates.add(criteriaBuilder.like(
					criteriaBuilder.lower(root.get("user").get("fullName")),
					"%" + searchName.toLowerCase() + "%"
				));
			}

			if (searchDepartment != null && !searchDepartment.isEmpty()) {
				predicates.add(criteriaBuilder.equal(root.get("department").get("id"), searchDepartment));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}
