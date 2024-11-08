package com.thanhnd.clinic_application.modules.doctors.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Doctor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends BaseRepository<Doctor, String> {
	@Query("SELECT d FROM Doctor d WHERE d.user.id = :userId")
	Optional<Doctor> findByUserId(String userId);
}
