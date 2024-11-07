package com.thanhnd.clinic_application.modules.shifts.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.RegisteredShift;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredShiftRepository extends BaseRepository<RegisteredShift, String> {
	@Query("SELECT rs FROM RegisteredShift rs WHERE rs.shift.id = :shiftId AND rs.doctor.id = :doctorId")
	Optional<RegisteredShift> findByShiftIdAndDoctorId(String shiftId, String doctorId);

	@Query("SELECT rs FROM RegisteredShift rs WHERE rs.shift.id = :shiftId AND rs.doctor.department.id = :departmentId")
	List<RegisteredShift> findAllByShiftIdAndDepartmentId(String shiftId, String departmentId);

	List<RegisteredShift> findAllByDoctorId(String doctorId);

	@Query("SELECT rs FROM RegisteredShift rs WHERE rs.doctor.id = :doctorId AND rs.shift.startTime >= :startTime AND rs.shift.endTime <= :endTime")
	List<RegisteredShift> findAllByDoctorIdAndShiftTimeBetween(String doctorId, Instant startTime, Instant endTime);
}
