package com.gptini.dto.response;

import com.gptini.enums.ChatRoomType;

import java.time.LocalDateTime;

public record ChatRoomListResponse(
        Long id,
        String name,
        ChatRoomType type,
        int userCount,
        String lastMessage,
        String lastMessageSender,
        LocalDateTime lastMessageTime,
        long unreadCount
) {}
