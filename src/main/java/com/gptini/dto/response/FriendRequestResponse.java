package com.gptini.dto.response;

import com.gptini.entity.FriendRequestEntity;
import com.gptini.entity.FriendRequestStatus;

import java.time.LocalDateTime;

public record FriendRequestResponse(
        Long id,
        UserResponse requester,
        FriendRequestStatus status,
        LocalDateTime createdAt
) {
    public static FriendRequestResponse from(FriendRequestEntity request) {
        return new FriendRequestResponse(
                request.getId(),
                UserResponse.from(request.getRequester()),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
