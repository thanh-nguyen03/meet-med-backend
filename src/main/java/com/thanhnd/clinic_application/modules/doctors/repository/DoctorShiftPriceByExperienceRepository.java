package com.thanhnd.clinic_application.modules.doctors.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.DoctorShiftPriceByExperience;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorShiftPriceByExperienceRepository extends BaseRepository<DoctorShiftPriceByExperience, String> {
	@Query("SELECT ds FROM DoctorShiftPriceByExperience ds ORDER BY ds.experience")
	List<DoctorShiftPriceByExperience> findAllSorted();
}
