package com.thanhnd.clinic_application.modules.shifts.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.RegisteredShiftTimeSlot;
import com.thanhnd.clinic_application.modules.shifts.dto.RegisteredShiftTimeSlotDto;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredShiftTimeSlotRepository extends BaseRepository<RegisteredShiftTimeSlot, RegisteredShiftTimeSlotDto> {
}
