package com.thanhnd.clinic_application.modules.chat.specification;

import com.thanhnd.clinic_application.entity.ChatBox;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChatBoxSpecification {
	public static Specification<ChatBox> filterNotUserAndDoctorNameLike(String userId, String doctorName) {
		return (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Not any member of the chat box is the user
			Predicate notUserPredicate = criteriaBuilder.notEqual(root.get("members").get("user").get("id"), userId);

			// The chat box has a member who is the doctor
			Predicate doctorNamePredicate = criteriaBuilder.like(
				criteriaBuilder.lower(root.get("members").get("user").get("fullName")),
				"%" + doctorName.toLowerCase() + "%"
			);

			predicates.add(notUserPredicate);
			predicates.add(doctorNamePredicate);

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}
}
