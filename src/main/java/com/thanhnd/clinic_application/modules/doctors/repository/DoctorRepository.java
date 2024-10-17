package com.thanhnd.clinic_application.modules.doctors.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Doctor;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends BaseRepository<Doctor, String> {
}
