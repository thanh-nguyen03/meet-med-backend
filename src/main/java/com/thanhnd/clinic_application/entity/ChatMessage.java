package com.thanhnd.clinic_application.entity;

import com.thanhnd.clinic_application.constants.ChatMessageStatus;
import com.thanhnd.clinic_application.constants.ChatMessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbl_chat_message")
public class ChatMessage extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ChatMessageType type = ChatMessageType.TEXT;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ChatMessageStatus status = ChatMessageStatus.SENT;

	@Column(name = "patient_id", insertable = false, updatable = false)
	private String patientId;

	@Column(name = "doctor_id", insertable = false, updatable = false)
	private String doctorId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
	private Doctor doctor;

	@ManyToOne
	@JoinColumn(name = "chat_box_id", referencedColumnName = "id", nullable = false)
	private ChatBox chatBox;
}
