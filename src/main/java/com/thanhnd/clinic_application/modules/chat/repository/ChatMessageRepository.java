package com.thanhnd.clinic_application.modules.chat.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends BaseRepository<ChatMessage, String> {
	Page<ChatMessage> findAllByChatBoxId(String chatBoxId, Pageable pageable);
}
