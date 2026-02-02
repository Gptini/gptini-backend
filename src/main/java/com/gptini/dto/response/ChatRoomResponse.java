package com.gptini.dto.response;

import com.gptini.entity.ChatRoomEntity;
import com.gptini.enums.ChatRoomType;

import java.time.ZonedDateTime;
import java.util.List;

public record ChatRoomResponse(
        Long id,
        String name,
        ChatRoomType type,
        ZonedDateTime createdAt,
        List<UserResponse> users,
        boolean isExisting
) {
    public static ChatRoomResponse from(ChatRoomEntity chatRoom) {
        return from(chatRoom, false);
    }

    public static ChatRoomResponse from(ChatRoomEntity chatRoom, boolean isExisting) {
        List<UserResponse> users = chatRoom.getUsers().stream()
                .map(cru -> UserResponse.from(cru.getUser()))
                .toList();

        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getName(),
                chatRoom.getType(),
                chatRoom.getCreatedAt(),
                users,
                isExisting
        );
    }
}
