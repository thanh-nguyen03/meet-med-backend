package com.thanhnd.clinic_application.modules.doctors.specification;

import com.thanhnd.clinic_application.entity.Doctor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class DoctorSpecification {
	public static Specification<Doctor> filterByNameAndDepartment(String search, String searchDepartment) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (search != null && !search.isEmpty()) {
				Predicate doctorNamePredicate = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("user").get("fullName")),
					"%" + search.toLowerCase() + "%"
				);

				Predicate departmentNamePredicate = criteriaBuilder.like(
					criteriaBuilder.lower(root.get("department").get("name")),
					"%" + search.toLowerCase() + "%"
				);

				predicates.add(criteriaBuilder.or(doctorNamePredicate, departmentNamePredicate));
			}

			if (searchDepartment != null && !searchDepartment.isEmpty()) {
				predicates.add(criteriaBuilder.equal(root.get("department").get("id"), searchDepartment));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}

	public static Specification<Doctor> filterByDepartments(List<String> departmentIds) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (departmentIds != null && !departmentIds.isEmpty()) {
				predicates.add(root.get("department").get("id").in(departmentIds));
			} else {
				predicates.add(criteriaBuilder.isNull(root.get("department").get("id")));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}
