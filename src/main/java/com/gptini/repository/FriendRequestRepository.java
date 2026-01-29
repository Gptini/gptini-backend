package com.gptini.repository;

import com.gptini.entity.FriendRequestEntity;
import com.gptini.entity.FriendRequestStatus;
import com.gptini.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Long> {

    List<FriendRequestEntity> findByReceiverAndStatus(UserEntity receiver, FriendRequestStatus status);

    List<FriendRequestEntity> findByRequesterAndStatus(UserEntity requester, FriendRequestStatus status);

    Optional<FriendRequestEntity> findByRequesterAndReceiver(UserEntity requester, UserEntity receiver);

    boolean existsByRequesterAndReceiverAndStatus(UserEntity requester, UserEntity receiver, FriendRequestStatus status);

    void deleteByRequesterOrReceiver(UserEntity requester, UserEntity receiver);
}
