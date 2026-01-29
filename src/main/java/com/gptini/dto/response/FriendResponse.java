package com.gptini.dto.response;

import com.gptini.entity.FriendshipEntity;

import java.time.LocalDateTime;

public record FriendResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        LocalDateTime friendSince
) {
    public static FriendResponse from(FriendshipEntity friendship) {
        return new FriendResponse(
                friendship.getFriend().getId(),
                friendship.getFriend().getEmail(),
                friendship.getFriend().getNickname(),
                friendship.getFriend().getProfileImageUrl(),
                friendship.getCreatedAt()
        );
    }
}
