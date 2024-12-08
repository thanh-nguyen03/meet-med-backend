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

	@ManyToOne
	@JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = false)
	private User sender;

	@ManyToOne
	@JoinColumn(name = "chat_box_id", referencedColumnName = "id", nullable = false)
	private ChatBox chatBox;
}
