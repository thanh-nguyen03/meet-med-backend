package com.thanhnd.clinic_application.modules.chat.repository;

import com.thanhnd.clinic_application.common.repository.BaseRepository;
import com.thanhnd.clinic_application.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMessageRepository extends BaseRepository<ChatMessage, String> {
	Optional<ChatMessage> findFirstByChatBoxIdOrderByCreatedAtDesc(String chatBoxId);

	Page<ChatMessage> findAllByChatBoxId(String chatBoxId, Pageable pageable);

	@Query("SELECT cm FROM ChatMessage cm WHERE cm.chatBox.id = :chatBoxId AND cm.status = 'READ'")
	Page<ChatMessage> findAllByChatBoxIdAndStatusRead(String chatBoxId, Pageable pageable);
}
