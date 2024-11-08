package com.thanhnd.clinic_application.modules.patients.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Patient;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends BaseRepository<Patient, String> {
	@Query("SELECT p FROM Patient p WHERE p.user.id = :userId")
	Optional<Patient> findByUserId(String userId);
}
