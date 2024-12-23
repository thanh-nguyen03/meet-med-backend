package com.thanhnd.clinic_application.modules.chat.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.ChatBox;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatBoxRepository extends BaseRepository<ChatBox, String> {
	Optional<ChatBox> findByPatientIdAndDoctorId(String patientId, String doctorId);
}
