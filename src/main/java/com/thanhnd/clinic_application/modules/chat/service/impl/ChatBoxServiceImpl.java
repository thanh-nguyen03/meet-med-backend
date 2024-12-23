package com.thanhnd.clinic_application.modules.chat.service.impl;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.Appointment;
import com.thanhnd.clinic_application.entity.ChatBox;
import com.thanhnd.clinic_application.entity.Doctor;
import com.thanhnd.clinic_application.entity.Patient;
import com.thanhnd.clinic_application.mapper.ChatBoxMapper;
import com.thanhnd.clinic_application.modules.appointments.repository.AppointmentRepository;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import com.thanhnd.clinic_application.modules.chat.repository.ChatBoxRepository;
import com.thanhnd.clinic_application.modules.chat.service.ChatBoxService;
import com.thanhnd.clinic_application.modules.chat.specification.ChatBoxSpecification;
import com.thanhnd.clinic_application.modules.doctors.repository.DoctorRepository;
import com.thanhnd.clinic_application.modules.patients.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatBoxServiceImpl implements ChatBoxService {
	private final ChatBoxRepository chatBoxRepository;
	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;
	private final PatientRepository patientRepository;

	private final ChatBoxMapper chatBoxMapper;

	private final JwtAuthenticationManager jwtAuthenticationManager;

	@Override
	public void createAllChatBox() {
		List<Appointment> appointments = appointmentRepository.findAll();
		for (Appointment appointment : appointments) {
			Patient patient = appointment.getPatient();
			String patientId = patient.getId();
			Doctor doctor = appointment.getRegisteredShiftTimeSlot().getRegisteredShift().getDoctor();
			String doctorId = doctor.getId();

			chatBoxRepository.findByPatientIdAndDoctorId(patientId, doctorId)
				.orElseGet(() -> {
					ChatBox newChatBox = new ChatBox();
					newChatBox.setPatient(patient);
					newChatBox.setDoctor(doctor);
					return chatBoxRepository.save(newChatBox);
				});
		}
	}

	@Override
	public PageableResultDto<ChatBoxDto> findAllByUser(Pageable pageable, String userId, String name) {
		Specification<ChatBox> specification = ChatBoxSpecification.filterName(name);
		Doctor doctor = doctorRepository.findByUserId(userId)
			.orElse(null);
		Patient patient = patientRepository.findByUserId(userId)
			.orElse(null);

		String doctorId = doctor != null ? doctor.getId() : null;
		String patientId = patient != null ? patient.getId() : null;

		Specification<ChatBox> userIdSpecification = ChatBoxSpecification.filterByUserId(patientId, doctorId);

		Specification<ChatBox> finalSpecification = userIdSpecification.and(specification);

		Page<ChatBox> chatBoxes = chatBoxRepository.findAll(finalSpecification, pageable);

		return PageableResultDto.parse(chatBoxes.map(chatBoxMapper::toDto));
	}

	@Override
	public ChatBoxDto findById(String id) {
		String userId = jwtAuthenticationManager.getUserId();
		ChatBox chatBox = chatBoxRepository.findById(id)
			.orElseThrow(() -> HttpException.notFound(Message.CHAT_BOX_NOT_FOUND.getMessage()));

		String patientUserId = chatBox.getPatient().getUser().getId();
		String doctorUserId = chatBox.getDoctor().getUser().getId();

		if (!userId.equals(patientUserId) && !userId.equals(doctorUserId)) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		return chatBoxMapper.toDto(chatBox);
	}
}
