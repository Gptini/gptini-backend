package com.gptini.repository;

import com.gptini.entity.FriendshipEntity;
import com.gptini.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, Long> {

    List<FriendshipEntity> findByUser(UserEntity user);

    @Query("SELECT f.friend FROM FriendshipEntity f WHERE f.user = :user")
    List<UserEntity> findFriendsByUser(@Param("user") UserEntity user);

    Optional<FriendshipEntity> findByUserAndFriend(UserEntity user, UserEntity friend);

    boolean existsByUserAndFriend(UserEntity user, UserEntity friend);

    void deleteByUserAndFriend(UserEntity user, UserEntity friend);

    void deleteByUserOrFriend(UserEntity user, UserEntity friend);
}
