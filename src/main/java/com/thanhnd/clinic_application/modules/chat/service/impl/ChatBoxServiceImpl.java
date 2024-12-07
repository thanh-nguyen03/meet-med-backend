package com.thanhnd.clinic_application.modules.chat.service.impl;

import com.thanhnd.clinic_application.common.dto.PageableResultDto;
import com.thanhnd.clinic_application.common.exception.HttpException;
import com.thanhnd.clinic_application.common.service.JwtAuthenticationManager;
import com.thanhnd.clinic_application.constants.Message;
import com.thanhnd.clinic_application.entity.ChatBox;
import com.thanhnd.clinic_application.entity.ChatBoxMember;
import com.thanhnd.clinic_application.mapper.ChatBoxMapper;
import com.thanhnd.clinic_application.modules.chat.dto.ChatBoxDto;
import com.thanhnd.clinic_application.modules.chat.repository.ChatBoxRepository;
import com.thanhnd.clinic_application.modules.chat.service.ChatBoxService;
import com.thanhnd.clinic_application.modules.chat.specification.ChatBoxSpecification;
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

	private final ChatBoxMapper chatBoxMapper;

	private final JwtAuthenticationManager jwtAuthenticationManager;

	@Override
	public PageableResultDto<ChatBoxDto> findAllByUser(Pageable pageable, String userId, String doctorName) {
		Specification<ChatBox> specification = ChatBoxSpecification.filterNotUserAndDoctorNameLike(userId, doctorName);
		Page<ChatBox> chatBoxes = chatBoxRepository.findAll(specification, pageable);

		return PageableResultDto.parse(chatBoxes.map(chatBoxMapper::toDtoExcludeMembers));
	}

	@Override
	public ChatBoxDto findById(String id) {
		String userId = jwtAuthenticationManager.getUserId();
		ChatBox chatBox = chatBoxRepository.findById(id)
			.orElseThrow(() -> HttpException.notFound(Message.CHAT_BOX_NOT_FOUND.getMessage()));

		List<ChatBoxMember> members = chatBox.getMembers();

		if (members.stream().noneMatch(member -> member.getUser().getId().equals(userId))) {
			throw HttpException.forbidden(Message.PERMISSION_DENIED.getMessage());
		}

		return chatBoxMapper.toDto(chatBox);
	}
}
