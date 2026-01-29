package com.gptini.dto.response;

import com.gptini.entity.UserEntity;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String friendCode
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getFriendCode()
        );
    }
}
