package com.gptini.repository;

import com.gptini.entity.ChatMessageEntity;
import com.gptini.entity.ChatMessageIdEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, ChatMessageIdEntity> {

    @Query("SELECT m FROM ChatMessageEntity m JOIN FETCH m.sender WHERE m.roomId = :roomId ORDER BY m.messageId DESC")
    List<ChatMessageEntity> findByRoomIdOrderByMessageIdDesc(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT m FROM ChatMessageEntity m JOIN FETCH m.sender WHERE m.roomId = :roomId AND m.messageId < :beforeId ORDER BY m.messageId DESC")
    List<ChatMessageEntity> findByRoomIdAndMessageIdLessThan(
            @Param("roomId") Long roomId,
            @Param("beforeId") Long beforeId,
            Pageable pageable
    );

    @Query("SELECT m FROM ChatMessageEntity m JOIN FETCH m.sender WHERE m.roomId = :roomId ORDER BY m.messageId DESC LIMIT 1")
    Optional<ChatMessageEntity> findLatestByRoomId(@Param("roomId") Long roomId);

    void deleteBySenderId(Long senderId);
}
