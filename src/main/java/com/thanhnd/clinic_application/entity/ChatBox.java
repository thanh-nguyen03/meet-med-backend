package com.thanhnd.clinic_application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tbl_chat_box")
public class ChatBox extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "socket_room_name", nullable = false, unique = true)
	private String socketRoomName;

	@OneToMany(mappedBy = "chatBox", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ChatBoxMember> members;
}
