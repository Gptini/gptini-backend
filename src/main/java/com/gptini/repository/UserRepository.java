package com.gptini.repository;

import com.gptini.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByIdIn(List<Long> ids);

    Optional<UserEntity> findByFriendCode(String friendCode);

    boolean existsByFriendCode(String friendCode);
}
