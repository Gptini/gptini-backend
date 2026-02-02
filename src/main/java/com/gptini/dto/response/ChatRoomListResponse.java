package com.gptini.dto.response;

import com.gptini.enums.ChatRoomType;

import java.time.ZonedDateTime;

public record ChatRoomListResponse(
        Long id,
        String name,
        ChatRoomType type,
        int userCount,
        String lastMessage,
        String lastMessageSender,
        ZonedDateTime lastMessageTime,
        long unreadCount
) {}
