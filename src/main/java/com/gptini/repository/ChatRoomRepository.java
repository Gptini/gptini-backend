package com.gptini.repository;

import com.gptini.entity.ChatRoomEntity;
import com.gptini.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    /**
     * 채팅방 조회 (users 페치조인)
     */
    @Query("SELECT cr FROM ChatRoomEntity cr LEFT JOIN FETCH cr.users u LEFT JOIN FETCH u.user WHERE cr.id = :roomId")
    Optional<ChatRoomEntity> findByIdWithUsers(@Param("roomId") Long roomId);

    /**
     * 두 사용자 간의 기존 1:1 채팅방 조회
     */
    @Query("""
        SELECT DISTINCT cr FROM ChatRoomEntity cr
        LEFT JOIN FETCH cr.users
        WHERE cr.type = :type
        AND EXISTS (SELECT 1 FROM ChatRoomUserEntity cru1 WHERE cru1.chatRoom = cr AND cru1.user.id = :userId1)
        AND EXISTS (SELECT 1 FROM ChatRoomUserEntity cru2 WHERE cru2.chatRoom = cr AND cru2.user.id = :userId2)
    """)
    Optional<ChatRoomEntity> findPrivateRoomByUsers(
            @Param("type") ChatRoomType type,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
}
