package com.gptini.repository;

import com.gptini.entity.ChatRoomMessageCounterEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMessageCounterRepository extends JpaRepository<ChatRoomMessageCounterEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChatRoomMessageCounterEntity c WHERE c.roomId = :roomId")
    Optional<ChatRoomMessageCounterEntity> findByRoomIdWithLock(@Param("roomId") Long roomId);

    @Query("SELECT c FROM ChatRoomMessageCounterEntity c WHERE c.roomId IN :roomIds")
    List<ChatRoomMessageCounterEntity> findByRoomIdIn(@Param("roomIds") List<Long> roomIds);
}
