package com.thanhnd.clinic_application.modules.symptom.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Symptom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomRepository extends BaseRepository<Symptom, String> {
	@Query("SELECT s FROM Symptom s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
	List<Symptom> findAllByNameContainingIgnoreCase(String name);
}
