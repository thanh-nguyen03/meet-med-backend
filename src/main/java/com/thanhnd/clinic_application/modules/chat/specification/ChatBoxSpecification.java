package com.thanhnd.clinic_application.modules.chat.specification;

import com.thanhnd.clinic_application.entity.ChatBox;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChatBoxSpecification {
	public static Specification<ChatBox> filterDoctorName(String doctorName) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// The chat box has a member who is the doctor
			Predicate doctorNamePredicate = criteriaBuilder.like(
				criteriaBuilder.lower(root.get("doctor").get("user").get("fullName")),
				"%" + doctorName.toLowerCase() + "%"
			);

			predicates.add(doctorNamePredicate);

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}
