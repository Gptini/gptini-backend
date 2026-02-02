package com.gptini.repository;

import com.gptini.entity.ChatRoomEntity;
import com.gptini.entity.ChatRoomUserEntity;
import com.gptini.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUserEntity, Long> {

    Optional<ChatRoomUserEntity> findByUserAndChatRoom(UserEntity user, ChatRoomEntity chatRoom);

    @Query("SELECT cru FROM ChatRoomUserEntity cru JOIN FETCH cru.chatRoom LEFT JOIN FETCH cru.chatRoom.users WHERE cru.user.id = :userId")
    List<ChatRoomUserEntity> findByUserIdWithChatRoom(@Param("userId") Long userId);

    @Query("SELECT cru FROM ChatRoomUserEntity cru JOIN FETCH cru.user WHERE cru.chatRoom.id = :chatRoomId")
    List<ChatRoomUserEntity> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    boolean existsByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    void deleteByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    void deleteByUserId(Long userId);
}
