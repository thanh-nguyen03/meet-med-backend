package com.thanhnd.clinic_application.modules.shifts.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.RegisteredShiftTimeSlot;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredShiftTimeSlotRepository extends BaseRepository<RegisteredShiftTimeSlot, String> {
}
