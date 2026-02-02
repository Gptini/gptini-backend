package com.gptini.dto.response;

import com.gptini.entity.FriendRequestEntity;
import com.gptini.entity.FriendRequestStatus;

import java.time.ZonedDateTime;

public record FriendRequestResponse(
        Long id,
        UserResponse requester,
        FriendRequestStatus status,
        ZonedDateTime createdAt
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
