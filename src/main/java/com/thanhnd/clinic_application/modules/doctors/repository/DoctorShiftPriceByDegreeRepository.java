package com.thanhnd.clinic_application.modules.doctors.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.DoctorShiftPriceByDegree;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorShiftPriceByDegreeRepository extends BaseRepository<DoctorShiftPriceByDegree, String> {
	DoctorShiftPriceByDegree findByDegree(String degree);
}
