package com.gptini.dto.response;

import com.gptini.entity.ChatRoomEntity;
import com.gptini.enums.ChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomResponse(
        Long id,
        String name,
        ChatRoomType type,
        LocalDateTime createdAt,
        List<UserResponse> users
) {
    public static ChatRoomResponse from(ChatRoomEntity chatRoom) {
        List<UserResponse> users = chatRoom.getUsers().stream()
                .map(cru -> UserResponse.from(cru.getUser()))
                .toList();

        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getName(),
                chatRoom.getType(),
                chatRoom.getCreatedAt(),
                users
        );
    }
}
