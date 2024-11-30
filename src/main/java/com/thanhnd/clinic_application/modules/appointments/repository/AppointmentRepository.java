package com.thanhnd.clinic_application.modules.appointments.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.Appointment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AppointmentRepository extends BaseRepository<Appointment, String> {
	List<Appointment> findAllByRegisteredShiftTimeSlotId(String registeredShiftTimeSlotId);

	@Query("SELECT a FROM Appointment a WHERE a.patient.user.id = :userId")
	List<Appointment> findAllByPatientUserId(String userId);

	@Query("SELECT a FROM Appointment a WHERE a.registeredShiftTimeSlot.registeredShift.id = :registeredShiftId")
	List<Appointment> findAllByRegisteredShiftId(String registeredShiftId);

	@Query("SELECT a FROM Appointment a " +
		"WHERE a.registeredShiftTimeSlot.startTime >= :startDateTime " +
		"AND a.registeredShiftTimeSlot.startTime <= :endDateTime"
	)
	List<Appointment> findAllByDateTimeBetween(Instant startDateTime, Instant endDateTime);
}
