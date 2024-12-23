package com.thanhnd.clinic_application.modules.chat.specification;

import com.thanhnd.clinic_application.entity.ChatBox;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChatBoxSpecification {
	public static Specification<ChatBox> filterName(String name) {
		return (root, query, criteriaBuilder) -> {
			if (name == null) {
				return criteriaBuilder.conjunction();
			}

			List<Predicate> predicates = new ArrayList<>();

			// The chat box has a member who is the doctor
			Predicate doctorNamePredicate = criteriaBuilder.like(
				criteriaBuilder.lower(root.get("doctor").get("user").get("fullName")),
				"%" + name.toLowerCase() + "%"
			);

			// The chat box has a member who is the patient
			Predicate patientNamePredicate = criteriaBuilder.like(
				criteriaBuilder.lower(root.get("patient").get("user").get("fullName")),
				"%" + name.toLowerCase() + "%"
			);

			predicates.add(doctorNamePredicate);
			predicates.add(patientNamePredicate);

			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		};
	}

	public static Specification<ChatBox> filterByUserId(String patientId, String doctorId) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (patientId != null) {
				Predicate patientIdPredicate = criteriaBuilder.equal(
					root.get("patient").get("id"),
					patientId
				);
				predicates.add(patientIdPredicate);
			}

			if (doctorId != null) {
				Predicate doctorIdPredicate = criteriaBuilder.equal(
					root.get("doctor").get("id"),
					doctorId
				);
				predicates.add(doctorIdPredicate);
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}
