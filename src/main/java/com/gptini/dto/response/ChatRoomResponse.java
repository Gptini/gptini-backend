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
        List<ChatRoomUserResponse> participants,
        boolean isExisting
) {
    public static ChatRoomResponse from(ChatRoomEntity chatRoom) {
        return from(chatRoom, false);
    }

    public static ChatRoomResponse from(ChatRoomEntity chatRoom, boolean isExisting) {
        List<ChatRoomUserResponse> participants = chatRoom.getUsers().stream()
                .map(ChatRoomUserResponse::from)
                .toList();

        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getName(),
                chatRoom.getType(),
                chatRoom.getCreatedAt(),
                participants,
                isExisting
        );
    }
}
